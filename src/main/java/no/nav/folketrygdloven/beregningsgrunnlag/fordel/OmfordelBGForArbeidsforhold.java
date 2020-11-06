package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
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
        BigDecimal refusjonskrav = arbeidMedOmfordelbartBg.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO);
        return bgForArbeidFratrektNaturalytelse.subtract(refusjonskrav);
    }

    private BigDecimal finnBgFratrektTilkommetNaturalytelse(BeregningsgrunnlagPrArbeidsforhold arbeidMedOmfordelbartBg) {
        return arbeidMedOmfordelbartBg.getBruttoPrÅr().orElse(BigDecimal.ZERO)
            .subtract(arbeidMedOmfordelbartBg.getNaturalytelseTilkommetPrÅr().orElse(BigDecimal.ZERO));
    }

}
