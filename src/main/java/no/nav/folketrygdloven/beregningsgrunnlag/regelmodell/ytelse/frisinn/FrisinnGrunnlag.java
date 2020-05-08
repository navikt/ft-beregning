package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class FrisinnGrunnlag extends YtelsesSpesifiktGrunnlag {

    private boolean erNyoppstartetFrilans;
    private boolean erSøktYtelseForFrilans;

    public FrisinnGrunnlag(boolean erNyoppstartetFrilans, boolean erSøktYtelseForFrilans) {
        super("FRISINN");
        this.erNyoppstartetFrilans = erNyoppstartetFrilans;
        this.erSøktYtelseForFrilans = erSøktYtelseForFrilans;
    }

    public boolean isErNyoppstartetFrilans() {
        return erNyoppstartetFrilans;
    }

    public boolean isErSøktYtelseForFrilans() {
        return erSøktYtelseForFrilans;
    }
}
