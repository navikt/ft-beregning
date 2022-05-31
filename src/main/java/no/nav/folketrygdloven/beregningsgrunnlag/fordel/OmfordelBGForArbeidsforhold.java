package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;

import java.math.BigDecimal;

class OmfordelBGForArbeidsforhold extends OmfordelForArbeidsforhold {

    OmfordelBGForArbeidsforhold(FordelModell beregningsgrunnlagPeriode) {
        super(beregningsgrunnlagPeriode);
    }

    @Override
    protected void flyttFraAktivitet(FordelAndelModell arbeidMedFlyttbartGrunnlag, BigDecimal beløpSomSkalFlyttes) {
        BigDecimal brutto = arbeidMedFlyttbartGrunnlag.getBruttoPrÅr().orElse(BigDecimal.ZERO);
	    FordelAndelModell.oppdater(arbeidMedFlyttbartGrunnlag).medFordeltPrÅr(brutto.subtract(beløpSomSkalFlyttes));
    }

    @Override
    protected BigDecimal finnFlyttbartBeløp(FordelAndelModell arbeidMedOmfordelbartBg) {
        BigDecimal bgForArbeidFratrektNaturalytelse = finnBgFratrektTilkommetNaturalytelse(arbeidMedOmfordelbartBg);
        BigDecimal refusjonskrav = arbeidMedOmfordelbartBg.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO);
        return bgForArbeidFratrektNaturalytelse.subtract(refusjonskrav);
    }

    private BigDecimal finnBgFratrektTilkommetNaturalytelse(FordelAndelModell arbeidMedOmfordelbartBg) {
        return arbeidMedOmfordelbartBg.getBruttoPrÅr().orElse(BigDecimal.ZERO)
            .subtract(arbeidMedOmfordelbartBg.getNaturalytelseTilkommetPrÅr().orElse(BigDecimal.ZERO));
    }

}
