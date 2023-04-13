package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.SammenligningGrunnlagType;

public class SammenligningsGrunnlag {
    private Periode sammenligningsperiode;
    private BigDecimal rapportertPrÅr;
    private BigDecimal avvikProsent = BigDecimal.ZERO;
	private SammenligningGrunnlagType sammenligningstype;

    private SammenligningsGrunnlag() {
        //Tom konstruktør
    }

    public Periode getSammenligningsperiode() {
        return sammenligningsperiode;
    }

    public BigDecimal getRapportertPrÅr() {
        return rapportertPrÅr;
    }

    public BigDecimal getAvvikPromilleUtenAvrunding() {
        return avvikProsent.scaleByPowerOfTen(1);
    }

    public BigDecimal getAvvikProsent() {
        return avvikProsent;
    }

	public SammenligningGrunnlagType getSammenligningstype() {
		return sammenligningstype;
	}

	public void setAvvikProsent(BigDecimal avvikProsent) {
        this.avvikProsent = avvikProsent;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SammenligningsGrunnlag mal;

        private Builder() {
            mal = new SammenligningsGrunnlag();
        }

        public Builder medSammenligningsperiode(Periode periode) {
            mal.sammenligningsperiode = periode;
            return this;
        }

        public Builder medRapportertPrÅr(BigDecimal rapportertPrÅr) {
            mal.rapportertPrÅr = rapportertPrÅr;
            return this;
        }

        public Builder medAvvikProsentFraPromille(long avvikPromille) {
            mal.avvikProsent = BigDecimal.valueOf(avvikPromille).scaleByPowerOfTen(-1);
            return this;
        }

        public Builder medAvvikProsentFraPromilleNy(BigDecimal avvikPromilleNy) {
            mal.avvikProsent = avvikPromilleNy.scaleByPowerOfTen(-1);
            return this;
        }

	    public Builder medSammenligningstype(SammenligningGrunnlagType type) {
		    mal.sammenligningstype = type;
		    return this;
	    }

        public Builder medAvvikProsent(BigDecimal avvikProsent) {
            mal.avvikProsent = avvikProsent;
            return this;
        }

        public SammenligningsGrunnlag build() {
            return mal;
        }
    }
}
