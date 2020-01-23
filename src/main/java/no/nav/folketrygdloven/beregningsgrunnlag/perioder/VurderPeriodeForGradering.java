package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

class VurderPeriodeForGradering {
    private VurderPeriodeForGradering() {
        // skjul public constructor
    }

    static List<PeriodeSplittData> vurder(PeriodeModell input, AndelGradering andelGradering, Periode gradering) {
        LocalDate graderingFom = gradering.getFom();
        LocalDate graderingTom = gradering.getTom();

        boolean totaltRefusjonskravStørreEnn6G = ErTotaltRefusjonskravStørreEnnEllerLikSeksG.vurder(input, graderingFom);
        if ((totaltRefusjonskravStørreEnn6G || andelGradering.erNyAktivitet())
            && !harRefusjonPåDato(andelGradering, graderingFom)) {
            return splittPeriodeGrunnetRefusjonOver6GEllerNyAktivitet(input, andelGradering, graderingFom, graderingTom);
        }
        return splittPeriodeGrunnetHøyerePrioriterteAndeler(input, andelGradering, gradering, graderingTom);
    }

    private static List<PeriodeSplittData> splittPeriodeGrunnetRefusjonOver6GEllerNyAktivitet(PeriodeModell input,
                                                                                              AndelGradering andelGradering,
                                                                                              LocalDate graderingFom,
                                                                                              LocalDate graderingTom) {
        PeriodeSplittData periodeSplitt = lagSplittForStartAvGradering(graderingFom);
        boolean totaltRefusjonskravStørreEnn6GVedOgEtterOpphørtGradering = ErTotaltRefusjonskravStørreEnnEllerLikSeksG.vurder(input, graderingTom)
            && (DateUtil.TIDENES_ENDE.isEqual(graderingTom) || ErTotaltRefusjonskravStørreEnnEllerLikSeksG.vurder(input, graderingTom.plusDays(1)));
        boolean harRefusjonskravVedOpphørtGradering = harRefusjonPåDato(andelGradering, graderingTom);
        if ((totaltRefusjonskravStørreEnn6GVedOgEtterOpphørtGradering || andelGradering.erNyAktivitet()) && !harRefusjonskravVedOpphørtGradering && !DateUtil.TIDENES_ENDE.isEqual(graderingTom)) {
            PeriodeSplittData opphørGraderingSplitt = lagSplittForOpphørAvGradering(graderingTom);
            return List.of(periodeSplitt, opphørGraderingSplitt);
        }
        return List.of(periodeSplitt);
    }

    private static List<PeriodeSplittData> splittPeriodeGrunnetHøyerePrioriterteAndeler(PeriodeModell input,
                                                                                        AndelGradering andelGradering,
                                                                                        Periode gradering,
                                                                                        LocalDate graderingTom) {
        Optional<LocalDate> høyerePrioriterteAndeler = IdentifiserPeriodeDerBruttoBgPåHøyerePrioriterteAndelerErMinst6G.vurder(input,
            andelGradering,
            gradering);
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

    private static boolean harRefusjonPåDato(AndelGradering andelGradering, LocalDate graderingFom) {
        return andelGradering.getGyldigeRefusjonskrav().stream()
            .filter(refusjonskrav -> refusjonskrav.getMånedsbeløp().compareTo(BigDecimal.ZERO) > 0)
            .anyMatch(refusjonskrav -> refusjonskrav.getPeriode().inneholder(graderingFom));
    }

}
