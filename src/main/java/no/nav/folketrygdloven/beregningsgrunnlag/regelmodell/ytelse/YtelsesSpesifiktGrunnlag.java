package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse;

public abstract class YtelsesSpesifiktGrunnlag {

    protected String ytelseType;

    public YtelsesSpesifiktGrunnlag(String ytelseType) {
        this.ytelseType = ytelseType;
    }

    public String getYtelseType() {
        return ytelseType;
    }
}
