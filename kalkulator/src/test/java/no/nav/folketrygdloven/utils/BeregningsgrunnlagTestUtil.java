package no.nav.folketrygdloven.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatusDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.fpsak.tidsserie.LocalDateInterval;


public class BeregningsgrunnlagTestUtil {

    public static BeregningsgrunnlagDto lagGjeldendeBeregningsgrunnlag(LocalDate skjæringstidspunktOpptjening,
                                                                       Optional<InntektArbeidYtelseGrunnlagDto> inntektArbeidYtelseGrunnlagDto,
                                                                       AktivitetStatus... statuser) {
        var avkortet = new HashMap<String, Integer>();
        var bruttoPrÅr = new HashMap<String, Integer>();
        var perioder = Collections.singletonList(new LocalDateInterval(skjæringstidspunktOpptjening, null));
        return lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, avkortet,
            bruttoPrÅr, Collections.emptyMap(), perioder, Collections.singletonList(Collections.emptyList()), Collections.emptyMap(), inntektArbeidYtelseGrunnlagDto, statuser);
    }

    public static BeregningsgrunnlagDto lagGjeldendeBeregningsgrunnlag(LocalDate skjæringstidspunktOpptjening,
                                                                       List<LocalDateInterval> berPerioder,
                                                                       InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                       AktivitetStatus... statuser) {
        return lagGjeldendeBeregningsgrunnlag(skjæringstidspunktOpptjening, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), berPerioder, Collections.emptyList(), Collections.emptyMap(), Optional.of(iayGrunnlag), statuser);
    }

    private static BeregningsgrunnlagDto lagGjeldendeBeregningsgrunnlag(// NOSONAR - brukes bare til test
                                                                        LocalDate skjæringstidspunktOpptjening,
                                                                        Map<String, Integer> andelAvkortet,
                                                                        Map<String, Integer> bruttoPrÅr,
                                                                        Map<String, List<Boolean>> lagtTilAvSaksbehandler,
                                                                        List<LocalDateInterval> perioder,
                                                                        List<List<PeriodeÅrsak>> periodePeriodeÅrsaker,
                                                                        Map<String, List<Inntektskategori>> inntektskategoriPrAndelIArbeidsforhold,
                                                                        Optional<InntektArbeidYtelseGrunnlagDto> inntektArbeidYtelseGrunnlag,
                                                                        AktivitetStatus... statuser) {
        var beregningsgrunnlag = BeregningsgrunnlagDto.builder()
            .medSkjæringstidspunkt(skjæringstidspunktOpptjening)
                .medGrunnbeløp(Beløp.fra(100_000))
            .build();

        if (statuser.length > 0) {
            byggBGForSpesifikkeAktivitetstatuser(inntektArbeidYtelseGrunnlag, skjæringstidspunktOpptjening, beregningsgrunnlag, statuser);
        } else {
            lagPerioder(andelAvkortet, bruttoPrÅr, lagtTilAvSaksbehandler, Collections.nCopies(perioder.size(), 1000),
                Collections.nCopies(perioder.size(), 1000), perioder, beregningsgrunnlag, periodePeriodeÅrsaker, inntektskategoriPrAndelIArbeidsforhold, Collections.emptyMap(), inntektArbeidYtelseGrunnlag);
        }
        return beregningsgrunnlag;
    }

    private static void byggBGForSpesifikkeAktivitetstatuser(Optional<InntektArbeidYtelseGrunnlagDto> inntektArbeidYtelseGrunnlag, LocalDate skjæringstidspunktOpptjening, BeregningsgrunnlagDto beregningsgrunnlag, AktivitetStatus[] statuser) {
        var bgAktivitetStatusbuilder = BeregningsgrunnlagAktivitetStatusDto.builder();
        for (var i = 1; i < statuser.length; i++) {
            bgAktivitetStatusbuilder.medAktivitetStatus(statuser[i]);
        }
        bgAktivitetStatusbuilder.medAktivitetStatus(statuser[0]).build(beregningsgrunnlag);
        var enkeltstatuser = oversettTilEnkeltstatuser(statuser);

        var periode = BeregningsgrunnlagPeriodeDto.ny()
            .medBeregningsgrunnlagPeriode(skjæringstidspunktOpptjening, null)
            .build(beregningsgrunnlag);
        for (var status : enkeltstatuser) {
            if (status.equals(AktivitetStatus.ARBEIDSTAKER)) {
                continue;
            }
            var andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.ny()
                .medAktivitetStatus(status);
            if (status.equals(AktivitetStatus.FRILANSER)) {
                andelBuilder.medBeregningsperiode(skjæringstidspunktOpptjening.minusMonths(3).withDayOfMonth(1), skjæringstidspunktOpptjening.withDayOfMonth(1).minusDays(1));
            }
            andelBuilder.build(periode);
        }
        if (inntektArbeidYtelseGrunnlag.isPresent()) {
            var agg = inntektArbeidYtelseGrunnlag.get();
            var aktørArbeid = agg.getAktørArbeidFraRegister();

            var filter = new YrkesaktivitetFilterDto(agg.getArbeidsforholdInformasjon(), aktørArbeid).før(skjæringstidspunktOpptjening);
            var aktiviteterOpt = filter.getYrkesaktiviteterForBeregning();

            var aktiviteter = aktiviteterOpt.stream().filter(a -> a.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()).collect(Collectors.toList());

            for (var i = 0; i < aktiviteter.size(); i++) {
                var arbId = aktiviteter.get(i).getArbeidsforholdRef().getReferanse();
                var orgNr = aktiviteter.get(i).getArbeidsgiver().getIdentifikator();
                var arbeidsperiode = finnArbeidsperiode(filter, aktiviteter, i);
                var bga = BGAndelArbeidsforholdDto.builder()
                    .medArbeidsgiver(lagArbeidsgiver(orgNr))
                    .medArbeidsforholdRef(arbId)
                    .medArbeidsperiodeFom(arbeidsperiode.getFomDato())
                    .medArbeidsperiodeTom(arbeidsperiode.getTomDato());

                BeregningsgrunnlagPrStatusOgAndelDto.ny()
                    .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                    .medBeregningsperiode(skjæringstidspunktOpptjening.minusMonths(3).withDayOfMonth(1), skjæringstidspunktOpptjening.withDayOfMonth(1).minusDays(1))
                    .medBGAndelArbeidsforhold(bga)
                    .build(periode);
            }
        }
    }

    private static Intervall finnArbeidsperiode(YrkesaktivitetFilterDto filter, List<YrkesaktivitetDto> aktiviteter, int i) {
        return filter.getAnsettelsesPerioder(aktiviteter.get(i)).stream()
            .map(a -> Intervall.fraOgMedTilOgMed(a.getPeriode().getFomDato(), a.getPeriode().getTomDato()))
            .reduce((p1, p2) -> {
                var fom = p1.getFomDato().isBefore(p2.getFomDato()) ? p1.getFomDato() : p2.getFomDato();
                var tom = p1.getTomDato().isAfter(p2.getTomDato()) ? p1.getTomDato() : p2.getTomDato();
                return tom == null ? Intervall.fraOgMed(fom) : Intervall.fraOgMedTilOgMed(fom, tom);
        }).orElse(Intervall.fraOgMedTilOgMed(LocalDate.now().minusYears(1), LocalDate.now().plusYears(2)));
    }

    private static List<AktivitetStatus> oversettTilEnkeltstatuser(AktivitetStatus... statuser) {
        List<AktivitetStatus> enkeltstatuser = new ArrayList<>();
        if (statuser.length == 0) {
            enkeltstatuser.add(AktivitetStatus.ARBEIDSTAKER);
        } else {
            Map<AktivitetStatus, List<AktivitetStatus>> kombinasjonsstatuser = new HashMap<>();
            kombinasjonsstatuser.put(AktivitetStatus.KOMBINERT_AT_FL, Arrays.asList(AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.FRILANSER));
            kombinasjonsstatuser.put(AktivitetStatus.KOMBINERT_AT_FL_SN, Arrays.asList(AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.FRILANSER, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));
            kombinasjonsstatuser.put(AktivitetStatus.KOMBINERT_AT_SN, Arrays.asList(AktivitetStatus.ARBEIDSTAKER, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));
            kombinasjonsstatuser.put(AktivitetStatus.KOMBINERT_FL_SN, Arrays.asList(AktivitetStatus.FRILANSER, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE));
            for (var status : statuser) {
                if (kombinasjonsstatuser.containsKey(status)) {
                    enkeltstatuser.addAll(kombinasjonsstatuser.get(status));
                } else {
                    enkeltstatuser.add(status);
                }
            }
        }
        return enkeltstatuser;
    }

    private static void lagPerioder(Map<String, Integer> avkortetAndel, // NOSONAR - brukes bare til test, men denne bør reskrives // TODO (Safir)
                                    Map<String, Integer> bruttoPrÅr,
                                    Map<String, List<Boolean>> lagtTilAvSaksbehandlerPrAndelIArbeidsforhold,
                                    List<Integer> redusert,
                                    List<Integer> avkortet,
                                    List<LocalDateInterval> perioder,
                                    BeregningsgrunnlagDto beregningsgrunnlag,
                                    List<List<PeriodeÅrsak>> periodePeriodeÅrsaker,
                                    Map<String, List<Inntektskategori>> inntektskategoriPrAndelIArbeidsforhold,
                                    Map<String, Integer> refusjonPrÅr,
                                    Optional<InntektArbeidYtelseGrunnlagDto> inntektArbeidYtelseGrunnlag) {
        var bgAktivitetStatusbuilder = BeregningsgrunnlagAktivitetStatusDto.builder();
        bgAktivitetStatusbuilder.medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).build(beregningsgrunnlag);
        for (var j = 0; j < perioder.size(); j++) {
            var periodeBuilder = BeregningsgrunnlagPeriodeDto.ny()
                .medBeregningsgrunnlagPeriode(perioder.get(j).getFomDato(), perioder.get(j).getTomDato())
                .medAvkortetPrÅr(avkortet.get(j) != null ? Beløp.fra(avkortet.get(j)) : null)
                .medRedusertPrÅr(redusert.get(j) != null ? Beløp.fra(redusert.get(j)) : null);
            if (!periodePeriodeÅrsaker.isEmpty()) {
                periodeBuilder.leggTilPeriodeÅrsaker(periodePeriodeÅrsaker.get(j));
            }
            var beregningsgrunnlagPeriode = periodeBuilder.build(beregningsgrunnlag);
            inntektArbeidYtelseGrunnlag.ifPresent(iayGrunnlag -> lagAndelerPrArbeidsforhold(beregningsgrunnlag, avkortetAndel, bruttoPrÅr, lagtTilAvSaksbehandlerPrAndelIArbeidsforhold,
                inntektskategoriPrAndelIArbeidsforhold, refusjonPrÅr, beregningsgrunnlagPeriode, iayGrunnlag));
        }
    }

    private static void lagAndelerPrArbeidsforhold(BeregningsgrunnlagDto beregningsgrunnlag, //NOSONAR
                                                   Map<String, Integer> avkortetAndel,
                                                   Map<String, Integer> bruttoPrÅr,
                                                   Map<String, List<Boolean>> lagtTilAvSaksbehandlerPrAndelIArbeidsforhold,
                                                   Map<String, List<Inntektskategori>> inntektskategoriPrAndelIArbeidsforhold,
                                                   Map<String, Integer> refusjonPrÅr,
                                                   BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode, InntektArbeidYtelseGrunnlagDto iayGrunnlag) {

        var aktørArbeid = iayGrunnlag.getAktørArbeidFraRegister();
        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), aktørArbeid);

        var aktiviteter = finnAlleYrkesaktiviteter(filter, beregningsgrunnlag);
        var arbeidsgivere = aktiviteter.stream()
            .map(YrkesaktivitetDto::getArbeidsgiver).collect(Collectors.toList());
        for (var i = 0; i < aktiviteter.size(); i++) {
            var arbId = aktiviteter.get(i).getArbeidsforholdRef().getReferanse();
            var arbeidsgiver = arbeidsgivere.get(i);
            var identifikator = arbeidsgiver.getIdentifikator();
            var aktivitetIPeriode = finnAktivitetForAndelIPeriode(filter, aktiviteter, beregningsgrunnlagPeriode.getPeriode(), identifikator);
            var arbeidsperiode = finnArbeidsperiode(filter, aktiviteter, i);
            var bga = byggArbeidsforhold(refusjonPrÅr, arbId, arbeidsgiver, arbeidsperiode);
            if (!aktivitetIPeriode.isEmpty()) {
                if (lagtTilAvSaksbehandlerPrAndelIArbeidsforhold.get(identifikator) != null && !lagtTilAvSaksbehandlerPrAndelIArbeidsforhold.get(identifikator).isEmpty()) {
                    for (var k = 0; k < lagtTilAvSaksbehandlerPrAndelIArbeidsforhold.get(identifikator).size(); k++) {
                        var inntektskategori = finnInntektskategori(inntektskategoriPrAndelIArbeidsforhold, identifikator, k);
                        byggAndel(avkortetAndel.get(identifikator), bruttoPrÅr.get(identifikator),
                            lagtTilAvSaksbehandlerPrAndelIArbeidsforhold.get(identifikator).get(k),
                            inntektskategori, beregningsgrunnlagPeriode, bga, beregningsgrunnlag.getSkjæringstidspunkt());
                    }
                } else {
                    byggAndel(avkortetAndel.get(identifikator), bruttoPrÅr.get(identifikator), false,
                        Inntektskategori.UDEFINERT, beregningsgrunnlagPeriode, bga, beregningsgrunnlag.getSkjæringstidspunkt());
                }
            }
        }
    }

    private static List<YrkesaktivitetDto> finnAlleYrkesaktiviteter(YrkesaktivitetFilterDto filter, BeregningsgrunnlagDto beregningsgrunnlag) {

        var filterFør = filter.før(beregningsgrunnlag.getSkjæringstidspunkt());
        var aktiviteterFørStpOpt = filterFør.getYrkesaktiviteterForBeregning();
        var aktiviteterFørStp = aktiviteterFørStpOpt.stream().filter(a -> a.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold());

        var filterEtter = filter.etter(beregningsgrunnlag.getSkjæringstidspunkt());
        var aktiviteterEtterStpOpt = filterEtter.getYrkesaktiviteterForBeregning();
        var aktiviteterEtterStp = aktiviteterEtterStpOpt.stream().filter(a -> a.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold());

        return Stream.concat(aktiviteterFørStp, aktiviteterEtterStp).distinct().collect(Collectors.toList());
    }

    private static List<YrkesaktivitetDto> finnAktivitetForAndelIPeriode(YrkesaktivitetFilterDto filter, List<YrkesaktivitetDto> aktiviteter, Intervall periode, String orgNr) {
        return aktiviteter.stream()
            .filter(a -> a.getArbeidsgiver().getIdentifikator().equals(orgNr))
            .filter(a -> filter.getAnsettelsesPerioder(a).stream().anyMatch(ansettelsePeriode ->
                ansettelsePeriode.getPeriode().overlapper(Intervall.fraOgMedTilOgMed(periode.getFomDato(), periode.getTomDato())) ||
                    ansettelsePeriode.getPeriode().getTomDato().isBefore(periode.getFomDato())))
            .collect(Collectors.toList());
    }

    private static Inntektskategori finnInntektskategori(Map<String, List<Inntektskategori>> inntektskategoriPrAndelIArbeidsforhold, String orgNr, int k) {
        return inntektskategoriPrAndelIArbeidsforhold.get(orgNr) != null && k < inntektskategoriPrAndelIArbeidsforhold.get(orgNr).size() ?
            inntektskategoriPrAndelIArbeidsforhold.get(orgNr).get(k) : Inntektskategori.ARBEIDSTAKER;
    }

    private static BGAndelArbeidsforholdDto.Builder byggArbeidsforhold(Map<String, Integer> refusjonPrÅr, String arbId, Arbeidsgiver arbeidsgiver, Intervall arbeidsperiode) {
        var identifikator = arbeidsgiver.getIdentifikator();
        return BGAndelArbeidsforholdDto.builder()
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidsforholdRef(arbId)
            .medRefusjonskravPrÅr(refusjonPrÅr.get(identifikator) != null ?
                    Beløp.fra(refusjonPrÅr.get(identifikator)) : null, Utfall.GODKJENT)
            .medArbeidsperiodeFom(arbeidsperiode.getFomDato())
            .medArbeidsperiodeTom(arbeidsperiode.getTomDato());
    }

    private static void byggAndel(Integer avkortetPrÅr, Integer bruttoPrÅr,
                                  Boolean lagtTilAvSaksbehandler,
                                  Inntektskategori inntektskategori,
                                  BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode,
                                  BGAndelArbeidsforholdDto.Builder bga, LocalDate skjæringstidspunkt) { //NOSONAR
        BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBeregningsperiode(skjæringstidspunkt.minusMonths(3).withDayOfMonth(1), skjæringstidspunkt.withDayOfMonth(1).minusDays(1))
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medAvkortetPrÅr(avkortetPrÅr != null ? Beløp.fra(avkortetPrÅr) : null)
            .medBeregnetPrÅr(bruttoPrÅr != null ? Beløp.fra(bruttoPrÅr) : null)
            .medKilde(lagtTilAvSaksbehandler ? AndelKilde.SAKSBEHANDLER_KOFAKBER : AndelKilde.PROSESS_START)
            .medInntektskategori(inntektskategori)
            .build(beregningsgrunnlagPeriode);
    }


    private static Arbeidsgiver lagArbeidsgiver(String orgNr) {
        return Arbeidsgiver.virksomhet(orgNr);
    }
}
