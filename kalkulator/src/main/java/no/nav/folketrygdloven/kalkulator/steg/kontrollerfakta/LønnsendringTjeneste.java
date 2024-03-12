package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;

public class LønnsendringTjeneste {

    private LønnsendringTjeneste() {
        // Skjul
    }

    /**
     * Finner aktiviteter som har lønnsendring i beregningsperioden
     *
     * @param beregningsgrunnlag Beregningsgrunnlag
     * @param iayGrunnlag        InntektArbeidYtelseGrunnlag
     * @return Liste med aktiviteter som har lønnsendring
     */
    public static List<YrkesaktivitetDto> finnAktiviteterMedLønnsendringUtenInntektsmeldingIBeregningsperiodenOgTilStp(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                                                       InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                                                       Collection<InntektsmeldingDto> inntektsmeldinger) {
        BiPredicate<BeregningsgrunnlagPrStatusOgAndelDto, LocalDate> datoErInkludertIRelevantPeriode = (andel, dato) -> {
            var stp = beregningsgrunnlag.getSkjæringstidspunkt();
            LocalDate førsteRelevanteDato = andel.getBeregningsperiodeFom().plusDays(1);  //lønnsendring første dag i bergningsperioden er OK, ser derfor fra og med dag 2
            Intervall relevantPeriode = Intervall.fraOgMedTilOgMed(førsteRelevanteDato, BeregningstidspunktTjeneste.finnBeregningstidspunkt(stp));
            return relevantPeriode.inkluderer(dato);
        };
        return finnYrkesaktiviteterMedLønnsendringUtenInnteksmeldingIBeregningsperiode(beregningsgrunnlag, iayGrunnlag, inntektsmeldinger, datoErInkludertIRelevantPeriode);
    }

    /**
     * Finner aktiviteter som har lønnsendring i uavkortet beregningsperiode (3  måneder før stp) og frem til skjæringstidspunktet
     *
     * @param beregningsgrunnlag Beregningsgrunnlag
     * @param iayGrunnlag        InntektArbeidYtelseGrunnlag
     * @return Liste med aktiviteter som har lønnsendring
     */
    public static List<YrkesaktivitetDto> finnAktiviteterMedLønnsendringUtenInntektsmeldingIStandardBeregningsperiodeOgTilStp(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                                                       InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                                                       Collection<InntektsmeldingDto> inntektsmeldinger) {
        BiPredicate<BeregningsgrunnlagPrStatusOgAndelDto, LocalDate> datoErInkludertIRelevantPeriode = (andel, dato) -> {
            var stp = beregningsgrunnlag.getSkjæringstidspunkt();
            LocalDate førsteRelevanteDato = stp.minusMonths(3).withDayOfMonth(2); //lønnsendring første dag i bergningsperioden er OK, ser derfor fra og med dag 2
            Intervall relevantPeriode = Intervall.fraOgMedTilOgMed(førsteRelevanteDato, BeregningstidspunktTjeneste.finnBeregningstidspunkt(stp));
            return relevantPeriode.inkluderer(dato);
        };
        return finnYrkesaktiviteterMedLønnsendringUtenInnteksmeldingIBeregningsperiode(beregningsgrunnlag, iayGrunnlag, inntektsmeldinger, datoErInkludertIRelevantPeriode);
    }


    public static List<YrkesaktivitetDto> finnAktiviteterMedLønnsendringUtenInntektsmelding(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                            InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                            Intervall periode,
                                                                                            Collection<InntektsmeldingDto> inntektsmeldinger) {
        LocalDate skjæringstidspunkt = beregningsgrunnlag.getSkjæringstidspunkt();

        Optional<AktørArbeidDto> aktørArbeid = iayGrunnlag.getAktørArbeidFraRegister();

        List<BeregningsgrunnlagPrStatusOgAndelDto> arbeidstakerAndeler = alleArbeidstakerandelerMedBeregningsperiode(beregningsgrunnlag);

        if (aktørArbeid.isEmpty() || arbeidstakerAndeler.isEmpty()) {
            return Collections.emptyList();
        }

        var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), aktørArbeid).før(skjæringstidspunkt);
        Collection<YrkesaktivitetDto> aktiviteterMedLønnsendring = finnAktiviteterMedLønnsendringIPerioden(filter, periode, skjæringstidspunkt);
        if (aktiviteterMedLønnsendring.isEmpty()) {
            return Collections.emptyList();
        }
        return aktiviteterMedLønnsendring.stream()
                .filter(ya -> ya.getArbeidsgiver() != null && ya.getArbeidsgiver().getIdentifikator() != null)
                .filter(ya -> finnesKorresponderendeBeregningsgrunnlagsandel(arbeidstakerAndeler, ya))
                .filter(ya -> manglerInntektsmelding(inntektsmeldinger, ya))
                .collect(Collectors.toList());
    }

    private static List<YrkesaktivitetDto> finnYrkesaktiviteterMedLønnsendringUtenInnteksmeldingIBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                                                   InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                                                   Collection<InntektsmeldingDto> inntektsmeldinger,
                                                                                                                   BiPredicate<BeregningsgrunnlagPrStatusOgAndelDto, LocalDate> harBeregningsperiodeSomOverlapperDato) {
        List<YrkesaktivitetDto> aktiviteterMedLønnsendring = new ArrayList<>();
        alleArbeidstakerandelerMedBeregningsperiode(beregningsgrunnlag).forEach(andel -> {
            Optional<AktørArbeidDto> aktørArbeid = iayGrunnlag.getAktørArbeidFraRegister();
            var filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), aktørArbeid);
            Optional<YrkesaktivitetDto> yrkesaktivitetMedLønnsendringIBeregningsperiode = finnMatchendeYrkesaktivitetMedLønnsendring(andel, filter, harBeregningsperiodeSomOverlapperDato);
            yrkesaktivitetMedLønnsendringIBeregningsperiode.ifPresent(ya -> {
                boolean manglerIM = manglerInntektsmelding(inntektsmeldinger, andel);
                if (manglerIM) {
                    aktiviteterMedLønnsendring.add(ya);
                }
            });
        });
        return aktiviteterMedLønnsendring;
    }

    private static Optional<YrkesaktivitetDto> finnMatchendeYrkesaktivitetMedLønnsendring(BeregningsgrunnlagPrStatusOgAndelDto andel,
                                                                                          YrkesaktivitetFilterDto filter,
                                                                                          BiPredicate<BeregningsgrunnlagPrStatusOgAndelDto, LocalDate> harBeregningsperiodeSomOverlapperDato) {
        return filter.getYrkesaktiviteterForBeregning().stream()
                .filter(ya -> andel.gjelderSammeArbeidsforhold(ya.getArbeidsgiver(), ya.getArbeidsforholdRef()))
                .filter(ya -> ya.getAlleAktivitetsAvtaler().stream().anyMatch(aa -> aa.getSisteLønnsendringsdato() != null
                        && harBeregningsperiodeSomOverlapperDato.test(andel, aa.getSisteLønnsendringsdato())))
                .findFirst();

    }

    private static boolean manglerInntektsmelding(Collection<InntektsmeldingDto> inntektsmeldinger, BeregningsgrunnlagPrStatusOgAndelDto andel) {
        return inntektsmeldinger.stream().noneMatch(im -> andel.gjelderSammeArbeidsforhold(im.getArbeidsgiver(), im.getArbeidsforholdRef()));
    }

    private static boolean manglerInntektsmelding(Collection<InntektsmeldingDto> inntektsmeldinger, YrkesaktivitetDto ya) {
        return inntektsmeldinger.stream().noneMatch(im -> ya.gjelderFor(im.getArbeidsgiver(), im.getArbeidsforholdRef()));
    }

    private static boolean finnesKorresponderendeBeregningsgrunnlagsandel(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler,
                                                                          YrkesaktivitetDto a) {
        return andeler.stream()
                .anyMatch(andel -> andel.gjelderSammeArbeidsforhold(a.getArbeidsgiver(), a.getArbeidsforholdRef()));
    }

    private static List<BeregningsgrunnlagPrStatusOgAndelDto> alleArbeidstakerandelerMedBeregningsperiode(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder().get(0)
                .getBeregningsgrunnlagPrStatusOgAndelList()
                .stream()
                .filter(bpsa -> bpsa.getKilde().equals(AndelKilde.PROSESS_START))
                .filter(bpsa -> bpsa.getAktivitetStatus().erArbeidstaker())
                .filter(bpsa -> bpsa.getBeregningsperiode() != null)
                .collect(Collectors.toList());
    }

    private static Collection<YrkesaktivitetDto> finnAktiviteterMedLønnsendringIPerioden(YrkesaktivitetFilterDto filter, Intervall periode, LocalDate skjæringstidspunkt) {
        return filter.getYrkesaktiviteterForBeregning()
                .stream()
                .filter(ya -> !ArbeidType.FRILANSER_OPPDRAGSTAKER.equals(ya.getArbeidType())
                        && !ArbeidType.FRILANSER.equals(ya.getArbeidType()))
                .filter(ya -> filter.getAnsettelsesPerioder(ya).stream()
                        .anyMatch(ap -> ap.getPeriode().inkluderer(skjæringstidspunkt)))
                .filter(ya -> harAvtalerMedLønnsendringIPerioden(filter.getAktivitetsAvtalerForArbeid(ya), periode))
                .collect(Collectors.toList());
    }

    private static boolean harAvtalerMedLønnsendringIPerioden(Collection<AktivitetsAvtaleDto> aktivitetsAvtaler, Intervall periode) {
        return aktivitetsAvtaler
                .stream()
                .filter(aa -> aa.getSisteLønnsendringsdato() != null)
                .filter(aa -> aa.getSisteLønnsendringsdato().equals(periode.getFomDato())
                        || aa.getSisteLønnsendringsdato().isAfter(periode.getFomDato()))
                .anyMatch(aa -> aa.getSisteLønnsendringsdato().equals(periode.getTomDato())
                        || aa.getSisteLønnsendringsdato().isBefore(periode.getTomDato()));
    }
}
