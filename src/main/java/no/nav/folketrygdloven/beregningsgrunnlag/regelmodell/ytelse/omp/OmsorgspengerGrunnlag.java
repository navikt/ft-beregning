package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;

public class OmsorgspengerGrunnlag extends YtelsesSpesifiktGrunnlag {

    private boolean erDirekteUtbetalingTilBruker;

    public OmsorgspengerGrunnlag(boolean erDirekteUtbetalingTilBruker) {
        super("OMP");
        this.erDirekteUtbetalingTilBruker = erDirekteUtbetalingTilBruker;
    }

    public boolean erDirekteUtbetalingTilBrukerIBeregningsgrunnlag() {
       return erDirekteUtbetalingTilBruker;
    }

}
