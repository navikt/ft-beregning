package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.RelatertYtelseType;
import no.nav.folketrygdloven.besteberegning.modell.BesteberegningRegelmodell;
import no.nav.folketrygdloven.besteberegning.modell.input.BesteberegningInput;
import no.nav.folketrygdloven.besteberegning.modell.input.YtelseAktivitetType;
import no.nav.folketrygdloven.besteberegning.modell.input.Ytelsegrunnlag;
import no.nav.folketrygdloven.besteberegning.modell.input.YtelsegrunnlagAndel;
import no.nav.folketrygdloven.besteberegning.modell.input.YtelsegrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.input.GrunnbeløpMapper;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.besteberegning.Ytelseandel;
import no.nav.folketrygdloven.kalkulator.modell.besteberegning.Ytelseperiode;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.OppgittEgenNæringDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

public class MapTilBesteberegningRegelmodell {

    public static BesteberegningRegelmodell map(ForeslåBesteberegningInput input) {
        BesteberegningInput besteberegningInput = lagInput(input);
        return new BesteberegningRegelmodell(besteberegningInput);
    }

    private static BesteberegningInput lagInput(ForeslåBesteberegningInput input) {
        List<Periodeinntekt> inntekter = mapInntekterForBesteberegning(input);
        Inntektsgrunnlag inntektsgrunnlag = new Inntektsgrunnlag();
        inntekter.forEach(inntektsgrunnlag::leggTilPeriodeinntekt);
        List<Periode> perioderMedNæringsvirksomhet = finnPerioderMedOppgittNæring(input);
        BesteberegningInput.Builder inputBuilder = BesteberegningInput.builder()
                .medInntektsgrunnlag(inntektsgrunnlag)
                .medGrunnbeløpSatser(GrunnbeløpMapper.mapGrunnbeløpInput(input.getGrunnbeløpInput()))
                .medGjeldendeGVerdi(finnGrunnbeløp(input))
                .medSkjæringstidspunktOpptjening(input.getSkjæringstidspunktOpptjening())
                .medPerioderMedNæringsvirksomhet(perioderMedNæringsvirksomhet)
                .medBeregnetGrunnlag(finnTotalBruttoUtenNaturalytelseFørstePeriode(input));
        mapYtelsegrunnlag(input.getYtelsespesifiktGrunnlag()).forEach(inputBuilder::leggTilYtelsegrunnlag);
        return inputBuilder.build();
    }

    private static List<Ytelsegrunnlag> mapYtelsegrunnlag(ForeldrepengerGrunnlag fpgrunnlag) {
        if (fpgrunnlag.getBesteberegningYtelsegrunnlag() == null) {
            return Collections.emptyList();
        }
        return fpgrunnlag.getBesteberegningYtelsegrunnlag().stream()
                .map(yg -> new Ytelsegrunnlag(mapYtelse(yg.ytelse()), mapYtelseperioder(yg.perioder())))
                .collect(Collectors.toList());
    }

    private static RelatertYtelseType mapYtelse(YtelseType ytelse) {
        return switch (ytelse) {
            case FORELDREPENGER -> RelatertYtelseType.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> RelatertYtelseType.SVANGERSKAPSPENGER;
            case SYKEPENGER -> RelatertYtelseType.SYKEPENGER;
            default -> throw new IllegalStateException("Fikk inn ukjent ytelsetype under mapping til besteberegning " + ytelse);
        };
    }

    private static List<YtelsegrunnlagPeriode> mapYtelseperioder(List<Ytelseperiode> perioder) {
        return perioder.stream()
                .map(p -> new YtelsegrunnlagPeriode(Periode.of(p.getPeriode().getFomDato(), p.getPeriode().getTomDato()), mapYtelseandeler(p.getAndeler())))
                .collect(Collectors.toList());
    }

    static List<YtelsegrunnlagAndel> mapYtelseandeler(List<Ytelseandel> andeler) {
        return andeler.stream()
                .map(a -> new YtelsegrunnlagAndel(mapTilYtelseAktivitetType(a), a.getDagsats() == null ? null : BigDecimal.valueOf(a.getDagsats())))
                .collect(Collectors.toList());
    }

    private static YtelseAktivitetType mapTilYtelseAktivitetType(Ytelseandel andel) {
        if (andel.getAktivitetStatus() != null) {
            return switch (andel.getAktivitetStatus()) {
                case ARBEIDSTAKER -> YtelseAktivitetType.YTELSE_FOR_ARBEID;
                case SELVSTENDIG_NÆRINGSDRIVENDE -> YtelseAktivitetType.YTELSE_FOR_NÆRING;
                case FRILANSER -> YtelseAktivitetType.YTELSE_FOR_FRILANS;
                case DAGPENGER -> YtelseAktivitetType.YTELSE_FOR_DAGPENGER;
                case ARBEIDSAVKLARINGSPENGER -> YtelseAktivitetType.YTELSE_FOR_ARBEIDSAVKLARINGSPENGER;
                case BRUKERS_ANDEL -> finnAktivitetstatusFraInntektskategori(andel);
                default -> throw new IllegalStateException("Fikk inn ukjent aktivitetstatus ved mapping til besteberegning, status var " + andel.getAktivitetStatus());
            };
        }
        else {
            return switch (andel.getArbeidskategori()) {
                case ARBEIDSTAKER, KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER, FISKER, SJØMANN -> YtelseAktivitetType.YTELSE_FOR_ARBEID;
                case SELVSTENDIG_NÆRINGSDRIVENDE, JORDBRUKER, DAGMAMMA -> YtelseAktivitetType.YTELSE_FOR_NÆRING;
                case FRILANSER -> YtelseAktivitetType.YTELSE_FOR_FRILANS;
                case DAGPENGER, INAKTIV -> YtelseAktivitetType.YTELSE_FOR_DAGPENGER;
                default -> throw new IllegalStateException("Fikk inn ukjent arbeidskategori ved mapping til besteberegning, kategori var " + andel.getArbeidskategori());
            };
        }
    }

    private static YtelseAktivitetType finnAktivitetstatusFraInntektskategori(Ytelseandel andel) {
        return switch (andel.getInntektskategori()) {
            case ARBEIDSTAKER, SJØMANN -> YtelseAktivitetType.YTELSE_FOR_ARBEID;
            case SELVSTENDIG_NÆRINGSDRIVENDE, DAGMAMMA, JORDBRUKER, FISKER -> YtelseAktivitetType.YTELSE_FOR_NÆRING;
            case FRILANSER -> YtelseAktivitetType.YTELSE_FOR_FRILANS;
            case ARBEIDSAVKLARINGSPENGER -> YtelseAktivitetType.YTELSE_FOR_ARBEIDSAVKLARINGSPENGER;
            case DAGPENGER, ARBEIDSTAKER_UTEN_FERIEPENGER -> YtelseAktivitetType.YTELSE_FOR_DAGPENGER;
            default -> throw new IllegalStateException("Fikk inn ukjent inntektskategori ved mapping til besteberegning, status var " + andel.getInntektskategori());
        };
    }

    /** Finner total brutto beregningsgrunnlag i første periode.
     * Tar ikke med naturalytelse ettersom filteret som brukes til å beregne de seks beste månedene heller ikke
     * tar med naturalytelse.
     *
     * @param input Input til foreslå besteberegning
     * @return Total brutto beregningsgrunnlag uten naturalytelse
     */
    private static BigDecimal finnTotalBruttoUtenNaturalytelseFørstePeriode(ForeslåBesteberegningInput input) {
        List<BeregningsgrunnlagPeriodeDto> perioder = input.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder();
        if (perioder.isEmpty()) {
            throw new IllegalStateException("Liste med perioder skal ikke vere tom");
        }
        BeregningsgrunnlagPeriodeDto førstePeriode = perioder.get(0);
        return Beløp.safeVerdi(førstePeriode.getBruttoPrÅr());
    }

    private static List<Periode> finnPerioderMedOppgittNæring(ForeslåBesteberegningInput input) {
        return input.getIayGrunnlag().getOppgittOpptjening().stream()
                    .flatMap(oo -> oo.getEgenNæring().stream())
                    .map(OppgittEgenNæringDto::getPeriode)
                    .map(p -> Periode.of(p.getFomDato(), p.getTomDato()))
                    .collect(Collectors.toList());
    }

    private static BigDecimal finnGrunnbeløp(ForeslåBesteberegningInput input) {
        return input.getBeregningsgrunnlagGrunnlag()
                .getBeregningsgrunnlagHvisFinnes().map(BeregningsgrunnlagDto::getGrunnbeløp).map(Beløp::verdi).orElseThrow(() -> new IllegalStateException("Forventer grunnbeløp"));
    }

    private static List<Periodeinntekt> mapInntekterForBesteberegning(BeregningsgrunnlagInput input) {
        var iayGrunnlag = input.getIayGrunnlag();
        var inntektFilterDto = new InntektFilterDto(iayGrunnlag.getAktørInntektFraRegister());
        var ytelseFilterDto = new YtelseFilterDto(iayGrunnlag.getAktørYtelseFraRegister());
        YrkesaktivitetFilterDto yrkesaktivitetFilterDto = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
        List<Periodeinntekt> inntekterATFLSN = finnInntekterATFLSN(input.getSkjæringstidspunktForBeregning(), inntektFilterDto, yrkesaktivitetFilterDto);
        List<Periodeinntekt> ytelserFraSammenligningsgrunnlaget = lagInntektForYtelseFraSammenligningsgrunnlag(inntektFilterDto);
        List<Periodeinntekt> ytelserDPOgAAP = MapArenaVedtakTilBesteberegningRegelmodell.lagInntektFraArenaYtelser(ytelseFilterDto);
        List<Periodeinntekt> alleInntekter = new ArrayList<>();
        alleInntekter.addAll(inntekterATFLSN);
        alleInntekter.addAll(ytelserFraSammenligningsgrunnlaget);
        alleInntekter.addAll(ytelserDPOgAAP);
        return alleInntekter;
    }




    /** Henter ut ytelser fra sammenlignigsgrunnlaget:
     * - FORELDREPENGER
     * - SVANGERSKAPSPENGER
     * - SYKEPENGER
     * - SYKEPENGER_FISKER
     *
     * @param filter Inntektsfilter
     * @return periodeinntekter for ytelser
     */
    private static List<Periodeinntekt> lagInntektForYtelseFraSammenligningsgrunnlag(InntektFilterDto filter) {
        return filter.filterSammenligningsgrunnlag().getFiltrertInntektsposter().stream()
                .filter(ip -> ip.getInntektYtelseType() != null)
                .map(e -> Periodeinntekt.builder()
                        .medInntektskildeOgPeriodeType(Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING)
                        .medAktivitetStatus(AktivitetStatus.KUN_YTELSE)
                        .medYtelse(mapTilRegelytelse(e))
                        .medMåned(e.getPeriode().getFomDato())
                        .medInntekt(e.getBeløp().verdi())
                        .build()).collect(Collectors.toList());
    }

    private static RelatertYtelseType mapTilRegelytelse(InntektspostDto dto) {
        return switch (dto.getInntektYtelseType()) {
            case FORELDREPENGER -> RelatertYtelseType.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> RelatertYtelseType.SVANGERSKAPSPENGER;
            case SYKEPENGER -> RelatertYtelseType.SYKEPENGER;
            // TODO: Fyll på med ytelser fra InntektYtelseType - det er mange nye
            default -> throw new IllegalStateException("Støtte på ukjent ytelse under besteberegning " + dto.getInntektYtelseType());
        };
    }


    private static List<Periodeinntekt> finnInntekterATFLSN(LocalDate skjæringstidspunktBeregning, InntektFilterDto filter, YrkesaktivitetFilterDto yrkesaktivitetFilterDto) {
        var filterYaRegister = yrkesaktivitetFilterDto.før(skjæringstidspunktBeregning);
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

    private static List<Periodeinntekt> lagInntektBeregning(InntektFilterDto filter,
                                                            Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        List<Periodeinntekt> inntekter = new ArrayList<>();
        filter.filterBeregningsgrunnlag()
                .filter(i -> i.getArbeidsgiver() != null)
                .forFilter((inntekt, inntektsposter) -> inntekter.addAll(mapInntekt(inntekt, inntektsposter, yrkesaktiviteter)));
        return inntekter;
    }

    private static List<Periodeinntekt> mapInntekt(InntektDto inntekt, Collection<InntektspostDto> inntektsposter,
                                                   Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        return inntektsposter.stream().map(inntektspost -> {

            Arbeidsforhold arbeidsgiver = mapYrkesaktivitet(inntekt.getArbeidsgiver(), yrkesaktiviteter);
            if (Objects.isNull(arbeidsgiver)) {
                throw new IllegalStateException("Arbeidsgiver må være satt.");
            } else if (Objects.isNull(inntektspost.getPeriode()) || Objects.isNull(inntektspost.getPeriode().getFomDato())) {
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

    private static Arbeidsforhold mapYrkesaktivitet(Arbeidsgiver arbeidsgiver, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        return erFrilanser(arbeidsgiver, yrkesaktiviteter)
                ? Arbeidsforhold.frilansArbeidsforhold()
                : lagNyttArbeidsforholdHosArbeidsgiver(arbeidsgiver);
    }

    private static boolean erFrilanser(Arbeidsgiver arbeidsgiver, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
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

    private static Arbeidsforhold lagNyttArbeidsforholdHosArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver.getErVirksomhet()) {
            return Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(arbeidsgiver.getIdentifikator());
        } else if (arbeidsgiver.erAktørId()) {
            return Arbeidsforhold.nyttArbeidsforholdHosPrivatperson(arbeidsgiver.getIdentifikator());
        }
        throw new IllegalStateException("Arbeidsgiver må være enten aktør eller virksomhet, men var: " + arbeidsgiver);
    }

    private static List<Periodeinntekt> lagInntekterSN(InntektFilterDto filter) {
        return filter.filterBeregnetSkatt().getFiltrertInntektsposter()
                .stream()
                .map(inntektspost -> Periodeinntekt.builder()
                        .medInntektskildeOgPeriodeType(Inntektskilde.SIGRUN)
                        .medAktivitetStatus(AktivitetStatus.SN)
                        .medInntekt(inntektspost.getBeløp().verdi())
                        .medPeriode(Periode.of(inntektspost.getPeriode().getFomDato(), inntektspost.getPeriode().getTomDato()))
                        .build()).collect(Collectors.toList());
    }

}
