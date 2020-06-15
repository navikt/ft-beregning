package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;

class OmfordelNaturalytelseForArbeidsforhold extends OmfordelForArbeidsforhold {

    OmfordelNaturalytelseForArbeidsforhold(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        super(beregningsgrunnlagPeriode);
    }

    @Override
    protected void flyttFraAktivitet(BeregningsgrunnlagPrArbeidsforhold arbeidMedFlyttbartGrunnlag, BigDecimal beløpSomSkalFlyttes) {
        BigDecimal bortfaltPrÅr = arbeidMedFlyttbartGrunnlag.getNaturalytelseBortfaltPrÅr().orElse(BigDecimal.ZERO);
        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidMedFlyttbartGrunnlag).medNaturalytelseBortfaltPrÅr(bortfaltPrÅr.subtract(beløpSomSkalFlyttes));
    }

    @Override
    protected BigDecimal finnFlyttbartBeløp(BeregningsgrunnlagPrArbeidsforhold arbeidMedOmfordelbartBg) {
        BigDecimal naturalytelseBortfaltPrÅr = arbeidMedOmfordelbartBg.getNaturalytelseBortfaltPrÅr().orElse(BigDecimal.ZERO);
        BigDecimal refusjonskrav = arbeidMedOmfordelbartBg.getRefusjonskravPrÅr().orElse(BigDecimal.ZERO);
        return naturalytelseBortfaltPrÅr.subtract(refusjonskrav).max(BigDecimal.ZERO);
    }

}
