package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;

public abstract class YtelsesSpesifiktGrunnlag {

    protected String ytelseType;
    protected Beregningsgrunnlag beregningsgrunnlag;

    public YtelsesSpesifiktGrunnlag(String ytelseType) {
        this.ytelseType = ytelseType;
    }

    public String getYtelseType() {
        return ytelseType;
    }

    public void setBeregningsgrunnlag(Beregningsgrunnlag beregningsgrunnlag) {
        this.beregningsgrunnlag = beregningsgrunnlag;
    }

}
