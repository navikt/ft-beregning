package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.fp;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class ForeldrepengerGrunnlag extends YtelsesSpesifiktGrunnlag {
    private boolean erBesteberegnet;

    public ForeldrepengerGrunnlag(boolean erBesteberegnet) {
        super("FP");
        this.erBesteberegnet = erBesteberegnet;
    }

    public boolean erBesteberegnet() {
        return erBesteberegnet;
    }
}
