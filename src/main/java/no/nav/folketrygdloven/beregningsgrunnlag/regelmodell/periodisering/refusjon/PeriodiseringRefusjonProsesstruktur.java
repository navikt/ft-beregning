package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

public class PeriodiseringRefusjonProsesstruktur {

    private PeriodeModellRefusjon input;
    private IdentifisertePeriodeÅrsaker identifisertePeriodeÅrsaker;
    private List<SplittetPeriode> splittetPerioder;

    public PeriodiseringRefusjonProsesstruktur(PeriodeModellRefusjon input) {
        this.input = input;
    }

    public PeriodeModellRefusjon getInput() {
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
