package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;

class OmfordelBGForArbeidsforhold extends OmfordelForArbeidsforhold {

    OmfordelBGForArbeidsforhold(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        super(beregningsgrunnlagPeriode);
    }

    @Override
    protected void flyttFraAktivitet(BeregningsgrunnlagPrArbeidsforhold arbeidMedFlyttbartGrunnlag, BigDecimal beløpSomSkalFlyttes) {
        BigDecimal brutto = arbeidMedFlyttbartGrunnlag.getBruttoPrÅr().orElse(BigDecimal.ZERO);
        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidMedFlyttbartGrunnlag).medFordeltPrÅr(brutto.subtract(beløpSomSkalFlyttes));
    }

    @Override
    protected BigDecimal finnFlyttbartBeløp(BeregningsgrunnlagPrArbeidsforhold arbeidMedOmfordelbartBg) {
        BigDecimal bgForArbeidFratrektNaturalytelse = finnBgFratrektTilkommetNaturalytelse(arbeidMedOmfordelbartBg);
        BigDecimal refusjonskrav = arbeidMedOmfordelbartBg.getRefusjonskravPrÅr().orElse(BigDecimal.ZERO);
        return bgForArbeidFratrektNaturalytelse.subtract(refusjonskrav);
    }

    private BigDecimal finnBgFratrektTilkommetNaturalytelse(BeregningsgrunnlagPrArbeidsforhold arbeidMedOmfordelbartBg) {
        return arbeidMedOmfordelbartBg.getBruttoPrÅr().orElse(BigDecimal.ZERO)
            .subtract(arbeidMedOmfordelbartBg.getNaturalytelseTilkommetPrÅr().orElse(BigDecimal.ZERO));
    }

}
