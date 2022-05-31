package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;

import java.math.BigDecimal;

class OmfordelNaturalytelseForArbeidsforhold extends OmfordelForArbeidsforhold {

    OmfordelNaturalytelseForArbeidsforhold(FordelModell beregningsgrunnlagPeriode) {
        super(beregningsgrunnlagPeriode);
    }

    @Override
    protected void flyttFraAktivitet(FordelAndelModell arbeidMedFlyttbartGrunnlag, BigDecimal beløpSomSkalFlyttes) {
        BigDecimal bortfaltPrÅr = arbeidMedFlyttbartGrunnlag.getNaturalytelseBortfaltPrÅr().orElse(BigDecimal.ZERO);
        FordelAndelModell.oppdater(arbeidMedFlyttbartGrunnlag).medNaturalytelseBortfaltPrÅr(bortfaltPrÅr.subtract(beløpSomSkalFlyttes));
    }

    @Override
    protected BigDecimal finnFlyttbartBeløp(FordelAndelModell arbeidMedOmfordelbartBg) {
        BigDecimal naturalytelseBortfaltPrÅr = arbeidMedOmfordelbartBg.getNaturalytelseBortfaltPrÅr().orElse(BigDecimal.ZERO);
        BigDecimal refusjonskrav = arbeidMedOmfordelbartBg.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO);
        return naturalytelseBortfaltPrÅr.subtract(refusjonskrav).max(BigDecimal.ZERO);
    }

}
