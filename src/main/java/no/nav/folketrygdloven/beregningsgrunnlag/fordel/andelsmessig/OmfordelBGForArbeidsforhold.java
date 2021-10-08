package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;

import java.math.BigDecimal;

class OmfordelBGForArbeidsforhold extends OmfordelForArbeidsforhold {

    OmfordelBGForArbeidsforhold(FordelPeriodeModell beregningsgrunnlagPeriode) {
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
