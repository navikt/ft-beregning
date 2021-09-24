package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.GraderingPrAktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;

class IdentifiserPerioderForGradering {
    private IdentifiserPerioderForGradering() {
        // skjul public constructor
    }

    static Set<PeriodeSplittData> identifiser(PeriodeModellGradering input, GraderingPrAktivitet graderinger) {
        Set<PeriodeSplittData> set = new HashSet<>();
	    graderinger.getPerioder().forEach(periode -> {
            List<PeriodeSplittData> splits = VurderPeriodeForGradering.vurder(
            		input,
		            graderinger,
		            periode);
            set.addAll(splits);
        });
        return set;
    }
}
