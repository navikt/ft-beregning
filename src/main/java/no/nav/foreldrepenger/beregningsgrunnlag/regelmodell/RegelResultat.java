package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegelResultat {

    private final ResultatBeregningType beregningsresultat;
    private final List<RegelMerknad> merknader = new ArrayList<>();
    private RegelSporing sporing;
    private RegelSporing sporing2;
    private RegelSporing sporingOppdatertGrunnlagSVP;

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

    public Optional<RegelSporing> getRegelSporingNr2() {
        return Optional.ofNullable(sporing2);
    }

    public Optional<RegelSporing> getRegelSporingOppdatertGrunnlagSVP() {
        return Optional.ofNullable(sporingOppdatertGrunnlagSVP);
    }

    public RegelResultat medRegelMerknad(RegelMerknad regelMerknad) {
        merknader.add(regelMerknad);
        return this;
    }

    public RegelResultat medRegelSporingNr2(String input, String sporing) {
        this.sporing2 = new RegelSporing(input, sporing);
        return this;
    }

    public RegelResultat medRegelSporingOppdatertGrunnlagSVP(String input, String sporing) {
        this.sporingOppdatertGrunnlagSVP = new RegelSporing(input, sporing);
        return this;
    }
}
