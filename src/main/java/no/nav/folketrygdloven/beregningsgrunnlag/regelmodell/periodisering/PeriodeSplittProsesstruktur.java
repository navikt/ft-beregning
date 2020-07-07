package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

public class PeriodeSplittProsesstruktur {

    private PeriodeModell input;
    private IdentifisertePeriodeÅrsaker identifisertePeriodeÅrsaker;
    private List<SplittetPeriode> splittetPerioder;

    public PeriodeSplittProsesstruktur(PeriodeModell input) {
        this.input = input;
    }

    public PeriodeModell getInput() {
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
