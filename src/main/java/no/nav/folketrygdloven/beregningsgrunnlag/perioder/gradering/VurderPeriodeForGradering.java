package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.GraderingPrAktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.Refusjon;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

class VurderPeriodeForGradering {
    private VurderPeriodeForGradering() {
        // skjul public constructor
    }

    static List<PeriodeSplittData> vurder(PeriodeModellGradering input, GraderingPrAktivitet gradering, Periode periode) {
        LocalDate graderingFom = periode.getFom();

        boolean totaltRefusjonskravStørreEnn6G = ErTotaltRefusjonskravStørreEnnEllerLikSeksG.vurder(input, graderingFom);

        if ((totaltRefusjonskravStørreEnn6G || gradering.erNyAktivitet())
            && !harRefusjonPåDato(input.getRefusjoner(), gradering, graderingFom)) {
            return splittPeriodeGrunnetRefusjonOver6GEllerNyAktivitet(input, gradering, periode);
        }
        if (!gradering.erNyAktivitet() && ErbruttoinntektForGradertAndelLikNull.vurder(input, gradering, graderingFom)){
            return splittPeriodeGrunnetBruttoinntektForAndelErNull(input, gradering, periode);
        }

        return splittPeriodeGrunnetHøyerePrioriterteAndeler(input, gradering, periode);
    }

    private static List<PeriodeSplittData> splittPeriodeGrunnetRefusjonOver6GEllerNyAktivitet(PeriodeModellGradering input,
                                                                                              GraderingPrAktivitet gradering, Periode periode) {
	    LocalDate graderingFom = periode.getFom();
	    LocalDate graderingTom = periode.getTom();
	    PeriodeSplittData periodeSplitt = lagSplittForStartAvGradering(graderingFom);
        boolean totaltRefusjonskravStørreEnn6GVedOgEtterOpphørtGradering = ErTotaltRefusjonskravStørreEnnEllerLikSeksG.vurder(input, graderingTom)
            && (DateUtil.TIDENES_ENDE.isEqual(graderingTom) || ErTotaltRefusjonskravStørreEnnEllerLikSeksG.vurder(input, graderingTom.plusDays(1)));
        boolean harRefusjonskravVedOpphørtGradering = harRefusjonPåDato(input.getRefusjoner(), gradering, periode.getFom());
        if ((totaltRefusjonskravStørreEnn6GVedOgEtterOpphørtGradering || gradering.erNyAktivitet()) && !harRefusjonskravVedOpphørtGradering && !DateUtil.TIDENES_ENDE.isEqual(graderingTom)) {
            PeriodeSplittData opphørGraderingSplitt = lagSplittForOpphørAvGradering(graderingTom);
            return List.of(periodeSplitt, opphørGraderingSplitt);
        }
        return List.of(periodeSplitt);
    }

    private static List<PeriodeSplittData> splittPeriodeGrunnetBruttoinntektForAndelErNull(PeriodeModellGradering input,
                                                                                           GraderingPrAktivitet gradering, Periode periode) {
	    LocalDate graderingFom = periode.getFom();
	    LocalDate graderingTom = periode.getTom();
        PeriodeSplittData periodeSplitt = lagSplittForStartAvGradering(graderingFom);
        boolean bruttoinntektErNullVedOgEtterOpphørtGradering = ErbruttoinntektForGradertAndelLikNull.vurder(input, gradering, graderingTom)
            && (DateUtil.TIDENES_ENDE.isEqual(graderingTom) || ErbruttoinntektForGradertAndelLikNull.vurder(input, gradering, graderingTom.plusDays(1)));
        if (bruttoinntektErNullVedOgEtterOpphørtGradering) {
            PeriodeSplittData opphørGraderingSplitt = lagSplittForOpphørAvGradering(graderingTom);
            return List.of(periodeSplitt, opphørGraderingSplitt);
        }
        return List.of(periodeSplitt);
    }

    private static List<PeriodeSplittData> splittPeriodeGrunnetHøyerePrioriterteAndeler(PeriodeModellGradering input,
                                                                                        GraderingPrAktivitet gradering, Periode periode) {
	    LocalDate graderingTom = periode.getTom();
        Optional<LocalDate> høyerePrioriterteAndeler = IdentifiserPeriodeDerBruttoBgPåHøyerePrioriterteAndelerErMinst6G.vurder(input,
            gradering
        );
        return høyerePrioriterteAndeler
            .map(fom -> {
                PeriodeSplittData graderingSplitt = lagSplittForStartAvGradering(fom);
                if (!DateUtil.TIDENES_ENDE.isEqual(graderingTom)) {
                    PeriodeSplittData endtGraderingSplitt = lagSplittForOpphørAvGradering(graderingTom);
                    return List.of(graderingSplitt, endtGraderingSplitt);
                }
                return List.of(graderingSplitt);
                }
            ).orElse(List.of());
    }

    private static PeriodeSplittData lagSplittForStartAvGradering(LocalDate fom) {
        return PeriodeSplittData.builder()
            .medPeriodeÅrsak(PeriodeÅrsak.GRADERING)
            .medFom(fom)
            .build();
    }

    private static PeriodeSplittData lagSplittForOpphørAvGradering(LocalDate graderingTom) {
        return PeriodeSplittData.builder()
            .medPeriodeÅrsak(PeriodeÅrsak.GRADERING_OPPHØRER)
            .medFom(graderingTom.plusDays(1))
            .build();
    }

    private static boolean harRefusjonPåDato(List<Refusjon> refusjoner,
                                             GraderingPrAktivitet gradering, LocalDate dato) {
    	return gradering.getArbeidsforhold() != null
			    && refusjoner.stream()
			    .filter(r -> r.getArbeidsforhold().matcherArbeidsforhold(gradering.getArbeidsforhold()))
			    .filter(refusjonskrav -> refusjonskrav.getBeløpPrÅr().compareTo(BigDecimal.ZERO) > 0)
			    .anyMatch(refusjonskrav -> refusjonskrav.getPeriode().inneholder(dato));
    }
}
