package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class FrisinnGrunnlag extends YtelsesSpesifiktGrunnlag {

    private boolean erNyoppstartetFrilans;

    public FrisinnGrunnlag(boolean erNyoppstartetFrilans) {
        super("FRISINN");
        this.erNyoppstartetFrilans = erNyoppstartetFrilans;
    }

    public boolean isErNyoppstartetFrilans() {
        return erNyoppstartetFrilans;
    }
}
