package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

class VurderPeriodeForGradering {
    private VurderPeriodeForGradering() {
        // skjul public constructor
    }

    static List<PeriodeSplittData> vurder(PeriodeModell input, AndelGradering andelGradering, Periode gradering) {
        LocalDate graderingFom = gradering.getFom();
        LocalDate graderingTom = gradering.getTom();

	    ArrayList<PeriodeSplittData> returnlist = new ArrayList<>();

	    if (skalSplitteVedDato(input, andelGradering, graderingFom)) {
		    returnlist.add(lagSplittFraDato(graderingFom, PeriodeÅrsak.GRADERING));
        }

	    if (skalSplitteVedDato(input, andelGradering, graderingTom)) {
		    returnlist.add(lagSplittFraDato(graderingTom.plusDays(1), PeriodeÅrsak.GRADERING_OPPHØRER));
	    }

	    if (returnlist.isEmpty()) {
		    returnlist.addAll(splittPeriodeGrunnetHøyerePrioriterteAndeler(input, andelGradering, gradering));
	    }

	    return returnlist;
    }

	private static boolean skalSplitteVedDato(PeriodeModell input, AndelGradering andelGradering, LocalDate dato) {
    	if (dato.equals(TIDENES_ENDE)) {
    		return false;
	    }
		boolean totaltRefusjonskravStørreEnn6G = ErTotaltRefusjonskravStørreEnnEllerLikSeksG.vurder(input, dato);
		if ((totaltRefusjonskravStørreEnn6G || andelGradering.erNyAktivitetPåDato(dato))
		    && !RefusjonForGraderingAndel.harRefusjonPåDato(andelGradering, input.getPeriodisertBruttoBeregningsgrunnlagList(), dato)) {
		    return true;
		}
		if (!RefusjonForGraderingAndel.harRefusjonPåDato(andelGradering, input.getPeriodisertBruttoBeregningsgrunnlagList(), dato) && ErbruttoinntektForGradertAndelLikNull.vurder(input, andelGradering, dato)){
		    return true;
		}
		return false;
	}

	private static PeriodeSplittData lagSplittFraDato(LocalDate fom, PeriodeÅrsak periodeÅrsak) {
		return PeriodeSplittData.builder()
				.medPeriodeÅrsak(periodeÅrsak)
				.medFom(fom)
				.build();
	}

	private static List<PeriodeSplittData> splittPeriodeGrunnetHøyerePrioriterteAndeler(PeriodeModell input,
	                                                                                    AndelGradering andelGradering,
	                                                                                    Periode gradering) {
		Optional<LocalDate> høyerePrioriterteAndeler = IdentifiserPeriodeDerBruttoBgPåHøyerePrioriterteAndelerErMinst6G.vurder(input,
				andelGradering,
				gradering);
		return høyerePrioriterteAndeler
				.map(fom -> {
							PeriodeSplittData graderingSplitt = lagSplittFraDato(fom, PeriodeÅrsak.GRADERING);
							if (!DateUtil.TIDENES_ENDE.isEqual(gradering.getTom())) {
								PeriodeSplittData endtGraderingSplitt = lagSplittFraDato(gradering.getTom().plusDays(1), PeriodeÅrsak.GRADERING_OPPHØRER);
								return List.of(graderingSplitt, endtGraderingSplitt);
							}
							return List.of(graderingSplitt);
						}
				).orElse(List.of());
	}



}
