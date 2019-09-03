package no.nav.foreldrepenger.beregningsgrunnlag.perioder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AndelGradering;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.PeriodeModell;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;

class IdentifiserPerioderForGradering {
    private IdentifiserPerioderForGradering() {
        // skjul public constructor
    }

    static Set<PeriodeSplittData> identifiser(PeriodeModell input, AndelGradering andelGradering) {
        Set<PeriodeSplittData> set = new HashSet<>();
        andelGradering.getGraderinger().forEach(gradering -> {
            List<PeriodeSplittData> splits = VurderPeriodeForGradering.vurder(input, andelGradering, gradering.getPeriode());
            splits.forEach(set::add);
        });
        return set;
    }
}
