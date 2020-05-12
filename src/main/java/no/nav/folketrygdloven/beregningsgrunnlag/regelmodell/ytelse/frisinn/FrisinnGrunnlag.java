package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class FrisinnGrunnlag extends YtelsesSpesifiktGrunnlag {

    private boolean erSøktYtelseForFrilans;

    public FrisinnGrunnlag(boolean erSøktYtelseForFrilans) {
        super("FRISINN");
        this.erSøktYtelseForFrilans = erSøktYtelseForFrilans;
    }

    public boolean isErSøktYtelseForFrilans() {
        return erSøktYtelseForFrilans;
    }
}
