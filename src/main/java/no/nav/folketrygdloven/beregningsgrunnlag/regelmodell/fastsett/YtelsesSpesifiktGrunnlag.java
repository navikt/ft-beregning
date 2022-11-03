package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett;

public abstract class YtelsesSpesifiktGrunnlag {

	protected String ytelseType;

	protected YtelsesSpesifiktGrunnlag(String ytelseType) {
		this.ytelseType = ytelseType;
	}

	public String getYtelseType() {
		return ytelseType;
	}

}
