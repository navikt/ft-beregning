package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;

class OmfordelBGForArbeidsforhold {

    private final BeregningsgrunnlagPeriode beregningsgrunnlagPeriode;

    OmfordelBGForArbeidsforhold(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        this.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
    }

    Map<String, Object> omfordelBGForArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold aktivitet, FinnArbeidsforholdMedOmfordelbartBeregningsgrunnlag finnArbeidsforhold) {
        BigDecimal beløpSomSkalOmfordelesTilAktivitet = aktivitet.getRefusjonskravPrÅr().orElse(BigDecimal.ZERO).subtract(aktivitet.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO));
        Map<String, Object> resultater = new HashMap<>();
        Optional<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdMedOmfordelbartBGOpt = finnArbeidsforhold.finn(beregningsgrunnlagPeriode);
        while (harMerÅOmfordele(beløpSomSkalOmfordelesTilAktivitet) && arbeidsforholdMedOmfordelbartBGOpt.isPresent()) {
            BeregningsgrunnlagPrArbeidsforhold arbeidMedOmfordelbartBg = arbeidsforholdMedOmfordelbartBGOpt.get();
            BigDecimal bgForArbeid = arbeidMedOmfordelbartBg.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO);
            BigDecimal refusjonskrav = arbeidMedOmfordelbartBg.getRefusjonskravPrÅr().orElse(BigDecimal.ZERO);
            BigDecimal omfordelbartBeløp = bgForArbeid.subtract(refusjonskrav);
            if (skalOmfordeleHeleGrunnlaget(beløpSomSkalOmfordelesTilAktivitet, omfordelbartBeløp)) {
                beløpSomSkalOmfordelesTilAktivitet = omfordelHeleGrunnlagetFraArbeidsforhold(aktivitet, beløpSomSkalOmfordelesTilAktivitet, arbeidMedOmfordelbartBg, refusjonskrav, omfordelbartBeløp);
            } else {
                beløpSomSkalOmfordelesTilAktivitet = omfordelDelerAvGrunnlagetFraArbeidsforhold(aktivitet, beløpSomSkalOmfordelesTilAktivitet, arbeidMedOmfordelbartBg, bgForArbeid);
            }
            resultater.put("fordeltPrÅr", arbeidMedOmfordelbartBg.getFordeltPrÅr());
            resultater.put("arbeidsforhold", arbeidMedOmfordelbartBg.getBeskrivelse());
            arbeidsforholdMedOmfordelbartBGOpt = finnArbeidsforhold.finn(beregningsgrunnlagPeriode);
        }
        return resultater;
    }

    private static BigDecimal omfordelDelerAvGrunnlagetFraArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold aktivitet, BigDecimal beløpSomSkalOmfordeles, BeregningsgrunnlagPrArbeidsforhold arbeidMedOmfordeltbartBg, BigDecimal bgForArbeid) {
        BigDecimal fordelt = bgForArbeid.subtract(beløpSomSkalOmfordeles);
        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidMedOmfordeltbartBg)
            .medFordeltPrÅr(fordelt);
        omfordelBGTilAktivitet(aktivitet, beløpSomSkalOmfordeles);
        beløpSomSkalOmfordeles = BigDecimal.ZERO;
        return beløpSomSkalOmfordeles;
    }

    private static BigDecimal omfordelHeleGrunnlagetFraArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold aktivitet, BigDecimal beløpSomSkalOmfordeles, BeregningsgrunnlagPrArbeidsforhold arbeidMedOmfordelbartBg, BigDecimal refusjonskrav, BigDecimal omfordelbartBeløp) {
        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidMedOmfordelbartBg)
            .medFordeltPrÅr(refusjonskrav);
        omfordelBGTilAktivitet(aktivitet, omfordelbartBeløp);
        beløpSomSkalOmfordeles = beløpSomSkalOmfordeles.subtract(omfordelbartBeløp);
        return beløpSomSkalOmfordeles;
    }

    private static boolean harMerÅOmfordele(BigDecimal beløpSomMåOmfordeles) {
        return beløpSomMåOmfordeles.compareTo(BigDecimal.ZERO) > 0;
    }

    private static void omfordelBGTilAktivitet(BeregningsgrunnlagPrArbeidsforhold aktivitet, BigDecimal beløpSomMåOmfordeles) {
        BeregningsgrunnlagPrArbeidsforhold.builder(aktivitet)
            .medFordeltPrÅr(aktivitet.getBruttoPrÅr().add(beløpSomMåOmfordeles));
    }

    private static boolean skalOmfordeleHeleGrunnlaget(BigDecimal restSomMåOmfordeles, BigDecimal omfordelbartBeløp) {
        return restSomMåOmfordeles.compareTo(omfordelbartBeløp) >= 0;
    }

    @FunctionalInterface
    interface FinnArbeidsforholdMedOmfordelbartBeregningsgrunnlag {
        Optional<BeregningsgrunnlagPrArbeidsforhold> finn(BeregningsgrunnlagPeriode periode);
    }
}
