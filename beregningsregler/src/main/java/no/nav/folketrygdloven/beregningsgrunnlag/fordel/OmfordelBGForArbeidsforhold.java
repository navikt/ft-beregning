package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.math.RoundingMode;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;

class OmfordelBGForArbeidsforhold extends OmfordelForArbeidsforhold {

	OmfordelBGForArbeidsforhold(FordelModell beregningsgrunnlagPeriode) {
		super(beregningsgrunnlagPeriode);
	}

	@Override
	protected void flyttFraAktivitet(FordelAndelModell arbeidMedFlyttbartGrunnlag, BigDecimal beløpSomSkalFlyttes) {
        var brutto = arbeidMedFlyttbartGrunnlag.getGradertBruttoPrÅr().orElse(BigDecimal.ZERO);
		FordelAndelModell.oppdater(arbeidMedFlyttbartGrunnlag).medFordeltPrÅr(skalerOpp(brutto.subtract(beløpSomSkalFlyttes), arbeidMedFlyttbartGrunnlag.getUtbetalingsgrad()));
	}

	private static BigDecimal skalerOpp(BigDecimal nyttFordeltBeløp, BigDecimal utbetalingsgrad) {
		if (nyttFordeltBeløp.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return nyttFordeltBeløp.multiply(BigDecimal.valueOf(100).divide(utbetalingsgrad, 10, RoundingMode.HALF_UP));
	}


	@Override
	protected BigDecimal finnFlyttbartBeløp(FordelAndelModell arbeidMedOmfordelbartBg) {
        var bgForArbeidFratrektNaturalytelse = finnBgFratrektTilkommetNaturalytelse(arbeidMedOmfordelbartBg);
        var refusjonskrav = arbeidMedOmfordelbartBg.getGradertRefusjonPrÅr().orElse(BigDecimal.ZERO);
		return bgForArbeidFratrektNaturalytelse.subtract(refusjonskrav);
	}

	private BigDecimal finnBgFratrektTilkommetNaturalytelse(FordelAndelModell arbeidMedOmfordelbartBg) {
		return arbeidMedOmfordelbartBg.getGradertBruttoPrÅr().orElse(BigDecimal.ZERO)
				.subtract(arbeidMedOmfordelbartBg.getGradertNaturalytelseTilkommetPrÅr().orElse(BigDecimal.ZERO));
	}

}
