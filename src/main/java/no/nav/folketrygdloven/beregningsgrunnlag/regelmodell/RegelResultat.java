package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegelResultat {

    private final ResultatBeregningType beregningsresultat;
    private final List<RegelMerknad> merknader = new ArrayList<>();
    private RegelSporing sporing;
    private RegelSporing sporingFinnGrenseverdi;

    public RegelResultat(ResultatBeregningType beregningsresultat, String regelInput, String regelSporing) {
        this.beregningsresultat = beregningsresultat;
        this.sporing = new RegelSporing(regelInput, regelSporing);
    }

    public List<RegelMerknad> getMerknader() {
        return merknader;
    }

    public ResultatBeregningType getBeregningsresultat() {
        return beregningsresultat;
    }

    public RegelSporing getRegelSporing() {
        return sporing;
    }

    public Optional<RegelSporing> getRegelSporingFinnGrenseverdi() {
        return Optional.ofNullable(sporingFinnGrenseverdi);
    }

    public RegelResultat medRegelMerknad(RegelMerknad regelMerknad) {
        merknader.add(regelMerknad);
        return this;
    }

    public RegelResultat medRegelsporingFinnGrenseverdi(String input, String sporing) {
        this.sporingFinnGrenseverdi = new RegelSporing(input, sporing);
        return this;
    }
}
