package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.psb.PleiepengerGrunnlag;

public abstract class YtelsesSpesifiktGrunnlag {

    protected String ytelseType;
    @JsonBackReference
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

	public boolean erKap9Ytelse() {
		return this instanceof OmsorgspengerGrunnlag ||
				this instanceof PleiepengerGrunnlag;
	}

}
