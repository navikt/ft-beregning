package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;

class OmfordelNaturalytelseForArbeidsforhold extends OmfordelForArbeidsforhold {

    OmfordelNaturalytelseForArbeidsforhold(FordelPeriodeModell beregningsgrunnlagPeriode) {
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
