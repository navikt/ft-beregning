package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

public class PeriodiseringGraderingProsesstruktur {

    private PeriodeModellGradering input;
    private IdentifisertePeriodeÅrsaker identifisertePeriodeÅrsaker;
    private List<SplittetPeriode> splittetPerioder;

    public PeriodiseringGraderingProsesstruktur(PeriodeModellGradering input) {
        this.input = input;
    }

    public PeriodeModellGradering getInput() {
        return input;
    }

    public IdentifisertePeriodeÅrsaker getIdentifisertePeriodeÅrsaker() {
        return identifisertePeriodeÅrsaker;
    }

    public List<SplittetPeriode> getSplittetPerioder() {
        return splittetPerioder;
    }

    public void setIdentifisertePeriodeÅrsaker(IdentifisertePeriodeÅrsaker identifisertePeriodeÅrsaker) {
        this.identifisertePeriodeÅrsaker = identifisertePeriodeÅrsaker;
    }

    public void setSplittetPerioder(List<SplittetPeriode> splittetPerioder) {
        this.splittetPerioder = splittetPerioder;
    }
}
