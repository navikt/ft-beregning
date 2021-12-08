package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse;

import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.perioder.naturalytelse.IdentifiserPeriodeÅrsakerNaturalytelse;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

public class PeriodiseringNaturalytelseProsesstruktur {

    private PeriodeModellNaturalytelse input;
    private IdentifiserteNaturalytelsePeriodeÅrsaker identifisertePeriodeÅrsaker;
    private List<SplittetPeriode> splittetPerioder;

    public PeriodiseringNaturalytelseProsesstruktur(PeriodeModellNaturalytelse input) {
        this.input = input;
    }

    public PeriodeModellNaturalytelse getInput() {
        return input;
    }

    public IdentifiserteNaturalytelsePeriodeÅrsaker getIdentifisertePeriodeÅrsaker() {
        return identifisertePeriodeÅrsaker;
    }

    public List<SplittetPeriode> getSplittetPerioder() {
        return splittetPerioder;
    }

    public void setIdentifisertePeriodeÅrsaker(IdentifiserteNaturalytelsePeriodeÅrsaker identifisertePeriodeÅrsaker) {
        this.identifisertePeriodeÅrsaker = identifisertePeriodeÅrsaker;
    }

    public void setSplittetPerioder(List<SplittetPeriode> splittetPerioder) {
        this.splittetPerioder = splittetPerioder;
    }
}
