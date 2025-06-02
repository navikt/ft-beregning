package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.util.HashSet;
import java.util.Set;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;

class IdentifiserPerioderForGradering {
    private IdentifiserPerioderForGradering() {
        // skjul public constructor
    }

    static Set<PeriodeSplittData> identifiser(PeriodeModellGradering input, AndelGradering andelGradering) {
        Set<PeriodeSplittData> set = new HashSet<>();
        andelGradering.getGraderinger().forEach(gradering -> {
            var splits = VurderPeriodeForGradering.vurder(input, andelGradering, gradering.getPeriode());
            set.addAll(splits);
        });
        return set;
    }
}
