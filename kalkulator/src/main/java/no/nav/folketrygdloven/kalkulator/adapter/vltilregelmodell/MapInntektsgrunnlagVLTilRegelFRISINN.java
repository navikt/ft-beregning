package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.kalkulator.felles.MeldekortUtils;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittFrilansInntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittOpptjeningDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittPeriodeInntekt;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

public class MapInntektsgrunnlagVLTilRegelFRISINN implements MapInntektsgrunnlagVLTilRegel {

    public static final int MÅNEDER_FØR_STP = 36;

    private static Periodeinntekt mapTilRegel(OppgittEgenNæringDto oppgittEgenNæringDto) {
        BigDecimal inntekt = Beløp.safeVerdi(oppgittEgenNæringDto.getInntekt());
        Intervall periode = oppgittEgenNæringDto.getPeriode();
        return Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.SØKNAD)
                .medPeriode(Periode.of(periode.getFomDato(), periode.getTomDato()))
                .medAktivitetStatus(AktivitetStatus.SN)
                .medInntekt(inntekt == null ? BigDecimal.ZERO : inntekt)
                .build();
    }

    public Inntektsgrunnlag map(BeregningsgrunnlagInput input, LocalDate skjæringstidspunkt) {
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        hentInntektArbeidYtelse(inntektsgrunnlag, input, skjæringstidspunkt);
        return inntektsgrunnlag;
    }

    private List<Periodeinntekt> lagInntektBeregning(InntektFilterDto filter,
                                                     Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        List<Periodeinntekt> inntekter = new ArrayList<>();
        filter.filterBeregningsgrunnlag()
                .filter(i -> i.getArbeidsgiver() != null)
                .forFilter((inntekt, inntektsposter) -> inntekter.addAll(mapInntekt(inntekt, inntektsposter, yrkesaktiviteter)));
        return inntekter;
    }

    private List<Periodeinntekt> mapInntekt(InntektDto inntekt, Collection<InntektspostDto> inntektsposter,
                                            Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        return inntektsposter.stream().map(inntektspost -> {

            Arbeidsforhold arbeidsgiver = mapYrkesaktivitet(inntekt.getArbeidsgiver(), yrkesaktiviteter);
            if (Objects.isNull(arbeidsgiver)) {
                throw new IllegalStateException("Arbeidsgiver må være satt.");
            } else if (Objects.isNull(inntektspost.getPeriode().getFomDato())) {
                throw new IllegalStateException("Inntektsperiode må være satt.");
            } else if (Objects.isNull(inntektspost.getBeløp().verdi())) {
                throw new IllegalStateException("Inntektsbeløp må være satt.");
            }
            return Periodeinntekt.builder()
                    .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING)
                    .medArbeidsgiver(arbeidsgiver)
                    .medMåned(inntektspost.getPeriode().getFomDato())
                    .medInntekt(inntektspost.getBeløp().verdi())
                    .build();
        }).collect(Collectors.toList());
    }

    private Arbeidsforhold mapYrkesaktivitet(Arbeidsgiver arbeidsgiver, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        return erFrilanser(arbeidsgiver, yrkesaktiviteter)
                ? Arbeidsforhold.frilansArbeidsforhold()
                : lagNyttArbeidsforholdHosArbeidsgiver(arbeidsgiver);
    }

    private boolean erFrilanser(Arbeidsgiver arbeidsgiver, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        final List<ArbeidType> arbeidType = yrkesaktiviteter
                .stream()
                .filter(it -> it.getArbeidsgiver() != null)
                .filter(it -> it.getArbeidsgiver().getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
                .map(YrkesaktivitetDto::getArbeidType)
                .distinct()
                .collect(Collectors.toList());
        boolean erFrilanser = yrkesaktiviteter.stream()
                .map(YrkesaktivitetDto::getArbeidType)
                .anyMatch(ArbeidType.FRILANSER::equals);
        return (arbeidType.isEmpty() && erFrilanser) || arbeidType.contains(ArbeidType.FRILANSER_OPPDRAGSTAKER);
    }

    private Arbeidsforhold lagNyttArbeidsforholdHosArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver.getErVirksomhet()) {
            return Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsgiver.getIdentifikator());
        } else if (arbeidsgiver.erAktørId()) {
            return Arbeidsforhold.nyttArbeidsforholdHosPrivatperson(arbeidsgiver.getIdentifikator());
        }
        throw new IllegalStateException("Arbeidsgiver må være enten aktør eller virksomhet, men var: " + arbeidsgiver);
    }

    private void mapAlleYtelser(Inntektsgrunnlag inntektsgrunnlag,
                                YtelseFilterDto ytelseFilter,
                                LocalDate skjæringstidspunktOpptjening) {
        ytelseFilter.getAlleYtelser().stream()
                .filter(y -> !y.getYtelseType().equals(YtelseType.FRISINN))
                .forEach(ytelse -> ytelse.getYtelseAnvist().stream()
                        .filter(ytelseAnvistDto -> !ytelseAnvistDto.getAnvistTOM().isBefore(skjæringstidspunktOpptjening.minusMonths(MÅNEDER_FØR_STP)))
                        .filter(this::harHattUtbetalingForPeriode)
                        .forEach(anvist -> inntektsgrunnlag.leggTilPeriodeinntekt(byggPeriodeinntektForYtelse(anvist, ytelse.getVedtaksDagsats(), ytelse.getYtelseType()))));
    }

    private boolean harHattUtbetalingForPeriode(YtelseAnvistDto ytelse) {
        return ytelse.getUtbetalingsgradProsent()
                .map(beløp -> !beløp.erNullEller0())
                .orElse(false);
    }

    private Periodeinntekt byggPeriodeinntektForYtelse(YtelseAnvistDto anvist, Optional<Beløp> vedtaksDagsats, YtelseType ytelsetype) {
        return Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(erAAPEllerDP(ytelsetype) ? Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP : Inntektskilde.ANNEN_YTELSE)
                .medInntekt(Beløp.safeVerdi(finnBeløp(anvist, vedtaksDagsats)))
                .medUtbetalingsfaktor(erAAPEllerDP(ytelsetype) ? anvist.getUtbetalingsgradProsent().map(Stillingsprosent::verdi)
                        .map(s -> s.divide(MeldekortUtils.MAX_UTBETALING_PROSENT_AAP_DAG, 10, RoundingMode.HALF_UP)).orElseThrow() : BigDecimal.ONE)
                .medPeriode(Periode.of(anvist.getAnvistFOM(), anvist.getAnvistTOM()))
                .build();
    }

    private boolean erAAPEllerDP(YtelseType ytelsetype) {
        return ytelsetype.equals(YtelseType.ARBEIDSAVKLARINGSPENGER) || ytelsetype.equals(YtelseType.DAGPENGER);
    }

    private Beløp finnBeløp(YtelseAnvistDto anvist, Optional<Beløp> vedtaksDagsats) {
        var beløpFraYtelseAnvist = anvist.getBeløp()
                .orElse(anvist.getDagsats().orElse(Beløp.ZERO));
        return vedtaksDagsats.orElse(beløpFraYtelseAnvist);
    }

    private List<Periodeinntekt> lagInntekterSN(InntektFilterDto filter) {
        return filter.filterBeregnetSkatt().getFiltrertInntektsposter()
                .stream()
                .map(inntektspost -> Periodeinntekt.builder()
                        .medInntektskildeOgPeriodeType(Inntektskilde.SIGRUN)
                        .medAktivitetStatus(AktivitetStatus.SN)
                        .medInntekt(inntektspost.getBeløp().verdi())
                        .medPeriode(Periode.of(inntektspost.getPeriode().getFomDato(), inntektspost.getPeriode().getTomDato()))
                        .build()).collect(Collectors.toList());
    }

    private void hentInntektArbeidYtelse(Inntektsgrunnlag inntektsgrunnlag, BeregningsgrunnlagInput input, LocalDate skjæringstidspunktBeregning) {
        InntektArbeidYtelseGrunnlagDto iayGrunnlag = input.getIayGrunnlag();


        YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag = input.getYtelsespesifiktGrunnlag();
        if (!(ytelsespesifiktGrunnlag instanceof FrisinnGrunnlag)) {
            throw new IllegalStateException("Ytelsesgrunnlag må være FRISINNgrunnlag for å beregne FRISINN ytelse");
        }
        FrisinnGrunnlag frisinnGrunnlag = (FrisinnGrunnlag) ytelsespesifiktGrunnlag;
        List<Periodeinntekt> samletInntekterATFLSN = finnInntekterATFLSN(skjæringstidspunktBeregning, iayGrunnlag);
        samletInntekterATFLSN.forEach(inntektsgrunnlag::leggTilPeriodeinntekt);

        var ytelseFilter = new YtelseFilterDto(iayGrunnlag.getAktørYtelseFraRegister()).før(input.getSkjæringstidspunktOpptjening());
        if (!ytelseFilter.getFiltrertYtelser().isEmpty()) {
            mapAlleYtelser(inntektsgrunnlag, ytelseFilter, input.getSkjæringstidspunktOpptjening());
        }

        Optional<OppgittOpptjeningDto> oppgittOpptjeningOpt = iayGrunnlag.getOppgittOpptjening();
        oppgittOpptjeningOpt.ifPresent(oppgittOpptjening ->
                mapOppgittOpptjening(inntektsgrunnlag, frisinnGrunnlag, oppgittOpptjening, input.getSkjæringstidspunktOpptjening()));

    }

    private List<Periodeinntekt> finnInntekterATFLSN(LocalDate skjæringstidspunktBeregning, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        var aktørArbeid = iayGrunnlag.getAktørArbeidFraRegister();
        var filterYaRegister = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), aktørArbeid).før(skjæringstidspunktBeregning);
        var filter = new InntektFilterDto(iayGrunnlag.getAktørInntektFraRegister());
        List<Periodeinntekt> samletInntekterATFLSN = new ArrayList<>();
        if (!filter.isEmpty()) {
            List<YrkesaktivitetDto> yrkesaktiviteter = new ArrayList<>();
            yrkesaktiviteter.addAll(filterYaRegister.getYrkesaktiviteterForBeregning());
            yrkesaktiviteter.addAll(filterYaRegister.getFrilansOppdrag());

            List<Periodeinntekt> inntekterATFL = lagInntektBeregning(filter, yrkesaktiviteter);
            List<Periodeinntekt> inntektNæring = lagInntekterSN(filter);

            samletInntekterATFLSN = Stream.concat(inntekterATFL.stream(), inntektNæring.stream()).collect(Collectors.toList());

        }
        return samletInntekterATFLSN;
    }

    private void mapOppgittOpptjening(Inntektsgrunnlag inntektsgrunnlag, FrisinnGrunnlag frisinnGrunnlag,
                                      OppgittOpptjeningDto oppgittOpptjening, LocalDate skjæringstidspunktBeregning) {
        mapOppgittNæringsinntekt(inntektsgrunnlag, frisinnGrunnlag, oppgittOpptjening, skjæringstidspunktBeregning);
        mapOppgittFrilansinntekt(inntektsgrunnlag, oppgittOpptjening, skjæringstidspunktBeregning);
        mapOppgittArbeidsinntekt(inntektsgrunnlag, oppgittOpptjening);
    }

    private void mapOppgittArbeidsinntekt(Inntektsgrunnlag inntektsgrunnlag, OppgittOpptjeningDto oppgittOpptjening) {
        List<OppgittPeriodeInntekt> inntekter = oppgittOpptjening.getOppgittArbeidsforhold()
                .stream().map(oppgittArbeidsforholdDto -> (OppgittPeriodeInntekt) oppgittArbeidsforholdDto)
                .collect(Collectors.toList());
        inntekter.forEach(inntekt -> inntektsgrunnlag.leggTilPeriodeinntekt(byggOppgittInntektForStatus(inntekt, AktivitetStatus.AT)));
    }

    private void mapOppgittFrilansinntekt(Inntektsgrunnlag inntektsgrunnlag, OppgittOpptjeningDto oppgittOpptjening, LocalDate skjæringstidspunktBeregning) {
        List<OppgittFrilansInntektDto> flInntekter = oppgittOpptjening.getFrilans()
                .map(OppgittFrilansDto::getOppgittFrilansInntekt)
                .orElse(Collections.emptyList());
        List<OppgittFrilansInntektDto> oppgittFLInntekt = flInntekter.stream()
                .filter(inntekt -> oppgittForPeriodeEtterSTP(inntekt.getPeriode(), skjæringstidspunktBeregning))
                .collect(Collectors.toList());
        oppgittFLInntekt.forEach(inntekt -> inntektsgrunnlag.leggTilPeriodeinntekt(byggOppgittInntektForStatus(inntekt, AktivitetStatus.FL)));
    }

    private Periodeinntekt byggOppgittInntektForStatus(OppgittPeriodeInntekt periodeInntekt, AktivitetStatus aktivitetStatus) {
        return Periodeinntekt.builder()
                .medInntektskildeOgPeriodeType(Inntektskilde.SØKNAD)
                .medPeriode(Periode.of(periodeInntekt.getPeriode().getFomDato(), periodeInntekt.getPeriode().getTomDato()))
                .medInntekt(Beløp.safeVerdi(periodeInntekt.getInntekt()))
                .medAktivitetStatus(aktivitetStatus)
                .build();
    }

    private boolean oppgittForPeriodeEtterSTP(Intervall periode, LocalDate skjæringstidspunktBeregning) {
        return !periode.getFomDato().isBefore(skjæringstidspunktBeregning);
    }

    private void mapOppgittNæringsinntekt(Inntektsgrunnlag inntektsgrunnlag,
                                          FrisinnGrunnlag frisinnGrunnlag,
                                          OppgittOpptjeningDto oppgittOpptjening, LocalDate skjæringstidspunktBeregning) {
        if (!oppgittOpptjening.getEgenNæring().isEmpty()) {
            Optional<OppgittEgenNæringDto> oppgittInntektFørStp = oppgittOpptjening.getEgenNæring().stream()
                    .filter(en -> skjæringstidspunktBeregning.isAfter(en.getFraOgMed()))
                    .findFirst();
            if (oppgittInntektFørStp.isEmpty() && frisinnGrunnlag.getSøkerYtelseForNæring()) {
                throw new IllegalStateException("Kunne ikke finne oppgitt næringsinntekt før skjæringstidspunkt, ugyldig tilstand for ytelse FRISINN");
            } else if (frisinnGrunnlag.getSøkerYtelseForNæring() && oppgittInntektFørStp.isPresent()) {
                inntektsgrunnlag.leggTilPeriodeinntekt(mapTilRegel(oppgittInntektFørStp.get()));
            }
            oppgittOpptjening.getEgenNæring().stream()
                    .filter(en -> !skjæringstidspunktBeregning.isAfter(en.getFraOgMed()))
                    .filter(en -> en.getBruttoInntekt() != null)
                    .map(MapInntektsgrunnlagVLTilRegelFRISINN::mapTilRegel)
                    .forEach(inntektsgrunnlag::leggTilPeriodeinntekt);
        }
    }
}
