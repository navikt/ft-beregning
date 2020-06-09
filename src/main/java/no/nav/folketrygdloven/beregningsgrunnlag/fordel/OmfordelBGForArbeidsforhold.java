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

class OmfordelBGForArbeidsforhold {

    private final BeregningsgrunnlagPeriode beregningsgrunnlagPeriode;

    OmfordelBGForArbeidsforhold(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        this.beregningsgrunnlagPeriode = beregningsgrunnlagPeriode;
    }

    Map<String, Object> omfordelBGForArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold aktivitet, FinnArbeidsforholdMedOmfordelbartBeregningsgrunnlag finnArbeidsforhold) {
        Map<String, Object> resultater = new HashMap<>();
        var refusjonskravFraArbeidsforhold = finnSamletBeløpFraArbeidsforhold(aktivitet.getArbeidsforhold(), BeregningsgrunnlagPrArbeidsforhold::getRefusjonskravPrÅr);
        var bruttoBgFraArbeidsforhold = finnSamletBeløpFraArbeidsforhold(aktivitet.getArbeidsforhold(), BeregningsgrunnlagPrArbeidsforhold::getBruttoInkludertNaturalytelsePrÅr);
        var resterendeBeløpÅFlytte = refusjonskravFraArbeidsforhold.subtract(bruttoBgFraArbeidsforhold);
        var arbforholdMedFlyttbartBeløpOpt = finnArbeidsforhold.finn(beregningsgrunnlagPeriode);
        while (harMerÅOmfordele(resterendeBeløpÅFlytte) && arbforholdMedFlyttbartBeløpOpt.isPresent()) {
            var arbeidMedFlyttbartBeløp = arbforholdMedFlyttbartBeløpOpt.get();
            var flyttbartBeløp = finnFlyttbartBeløp(arbeidMedFlyttbartBeløp);
            if (skalOmfordeleHeleGrunnlaget(resterendeBeløpÅFlytte, flyttbartBeløp)) {
                resterendeBeløpÅFlytte = flyttBGFraAktivitetTilArbeid(aktivitet, resterendeBeløpÅFlytte, arbeidMedFlyttbartBeløp, flyttbartBeløp);
            } else {
                resterendeBeløpÅFlytte = flyttBGFraAktivitetTilArbeid(aktivitet, resterendeBeløpÅFlytte, arbeidMedFlyttbartBeløp, resterendeBeløpÅFlytte);
            }
            resultater.put("fordeltPrÅr", arbeidMedFlyttbartBeløp.getFordeltPrÅr());
            resultater.put("arbeidsforhold", arbeidMedFlyttbartBeløp.getBeskrivelse());
            arbforholdMedFlyttbartBeløpOpt = finnArbeidsforhold.finn(beregningsgrunnlagPeriode);
        }
        return resultater;
    }

    private BigDecimal finnFlyttbartBeløp(BeregningsgrunnlagPrArbeidsforhold arbeidMedOmfordelbartBg) {
        BigDecimal bgForArbeidFratrektNaturalytelse = finnBgFratrektTilkommetNaturalytelse(arbeidMedOmfordelbartBg);
        BigDecimal refusjonskrav = arbeidMedOmfordelbartBg.getRefusjonskravPrÅr().orElse(BigDecimal.ZERO);
        return bgForArbeidFratrektNaturalytelse.subtract(refusjonskrav);
    }

    private BigDecimal finnBgFratrektTilkommetNaturalytelse(BeregningsgrunnlagPrArbeidsforhold arbeidMedOmfordelbartBg) {
        return arbeidMedOmfordelbartBg.getBruttoPrÅr().orElse(BigDecimal.ZERO)
            .subtract(arbeidMedOmfordelbartBg.getNaturalytelseTilkommetPrÅr().orElse(BigDecimal.ZERO));
    }

    private BigDecimal finnSamletBeløpFraArbeidsforhold(Arbeidsforhold arbeidsforhold, Function<BeregningsgrunnlagPrArbeidsforhold, Optional<BigDecimal>> getBeløpOptional) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL).getArbeidsforhold().stream()
            .filter(a -> a.getArbeidsforhold().equals(arbeidsforhold))
            .map(getBeløpOptional)
            .filter(Optional::isPresent).map(Optional::get)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private void adderBeløpTilRefusjonForArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold eksisterende, BeregningsgrunnlagPrArbeidsforhold aktivitet, BigDecimal beløpSomSkalOmfordelesTilArbeidsforhold) {
        if (erEksisterende(aktivitet)) {
            return;
        }
        if (eksisterende.getRefusjonskravPrÅr().isEmpty()) {
            throw new IllegalStateException("Eksisterende andel har ikke refusjonskrav.");
        }
        if (eksisterende.getRefusjonskravPrÅr().get().compareTo(beløpSomSkalOmfordelesTilArbeidsforhold) < 0) {
            throw new IllegalStateException("Skal ikke flytte mer av refusjonskravet.");
        }
        BeregningsgrunnlagPrArbeidsforhold.builder(eksisterende)
            .medRefusjonskravPrÅr(eksisterende.getRefusjonskravPrÅr().get().subtract(beløpSomSkalOmfordelesTilArbeidsforhold));
        BeregningsgrunnlagPrArbeidsforhold.builder(aktivitet)
            .medRefusjonskravPrÅr(aktivitet.getRefusjonskravPrÅr().isPresent() ? aktivitet.getRefusjonskravPrÅr().get().add(beløpSomSkalOmfordelesTilArbeidsforhold) : beløpSomSkalOmfordelesTilArbeidsforhold);
    }

    private BeregningsgrunnlagPrArbeidsforhold finnEksisterende(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode, Arbeidsforhold arbeidsforhold) {
        return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL)
            .getArbeidsforhold().stream()
            .filter(a -> a.getArbeidsforhold().equals(arbeidsforhold) && erEksisterende(a))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Fant ikke eksisterende BeregningsgrunnlagPrArbeidsforhold for " + arbeidsforhold));
    }

    private boolean erEksisterende(BeregningsgrunnlagPrArbeidsforhold a) {
        return a.getAndelNr() != null;
    }

    private BigDecimal flyttBGFraAktivitetTilArbeid(BeregningsgrunnlagPrArbeidsforhold arbeid,
                                                    BigDecimal restÅFlytte,
                                                    BeregningsgrunnlagPrArbeidsforhold arbeidMedFlyttbartGrunnlag,
                                                    BigDecimal beløpSomSkalFlyttes) {
        BigDecimal brutto = arbeidMedFlyttbartGrunnlag.getBruttoPrÅr().orElse(BigDecimal.ZERO);
        BeregningsgrunnlagPrArbeidsforhold.builder(arbeidMedFlyttbartGrunnlag).medFordeltPrÅr(brutto.subtract(beløpSomSkalFlyttes));
        omfordelBGTilAktivitet(arbeid, beløpSomSkalFlyttes);
        adderBeløpTilRefusjonForArbeidsforhold(finnEksisterende(beregningsgrunnlagPeriode, arbeid.getArbeidsforhold()), arbeid, beløpSomSkalFlyttes);
        restÅFlytte = restÅFlytte.subtract(beløpSomSkalFlyttes);
        return restÅFlytte;
    }

    private static boolean harMerÅOmfordele(BigDecimal beløpSomMåOmfordeles) {
        return beløpSomMåOmfordeles.compareTo(BigDecimal.ZERO) > 0;
    }

    private static void omfordelBGTilAktivitet(BeregningsgrunnlagPrArbeidsforhold aktivitet, BigDecimal beløpSomMåOmfordeles) {
        BeregningsgrunnlagPrArbeidsforhold.builder(aktivitet).medFordeltPrÅr(aktivitet.getBruttoPrÅr().orElse(BigDecimal.ZERO).add(beløpSomMåOmfordeles));
    }

    private static boolean skalOmfordeleHeleGrunnlaget(BigDecimal restSomMåOmfordeles, BigDecimal omfordelbartBeløp) {
        return restSomMåOmfordeles.compareTo(omfordelbartBeløp) >= 0;
    }

    @FunctionalInterface
    interface FinnArbeidsforholdMedOmfordelbartBeregningsgrunnlag {
        Optional<BeregningsgrunnlagPrArbeidsforhold> finn(BeregningsgrunnlagPeriode periode);
    }
}
