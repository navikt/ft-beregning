package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

abstract class OmfordelForArbeidsforhold {

    private final FordelModell modell;

    OmfordelForArbeidsforhold(FordelModell modell) {
        this.modell = modell;
    }

    Map<String, Object> omfordelForArbeidsforhold(FordelAndelModell aktivitet, FinnArbeidsforholdMedOmfordelbartGrunnlag finnArbeidsforhold) {
        Map<String, Object> resultater = new HashMap<>();
        var refusjonskravFraArbeidsforhold = finnSamletBeløpFraArbeidsforhold(aktivitet.getArbeidsforhold(), FordelAndelModell::getGradertRefusjonPrÅr);
        var bruttoBgFraArbeidsforhold = finnSamletBeløpFraArbeidsforhold(aktivitet.getArbeidsforhold(), FordelAndelModell::getGradertBruttoInkludertNaturalytelsePrÅr);
        var resterendeBeløpÅFlytte = refusjonskravFraArbeidsforhold.subtract(bruttoBgFraArbeidsforhold);
        var arbforholdMedFlyttbartBeløpOpt = finnArbeidsforhold.finn(modell.getInput());
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
            arbforholdMedFlyttbartBeløpOpt = finnArbeidsforhold.finn(modell.getInput());
        }
        return resultater;
    }

    protected abstract BigDecimal finnFlyttbartBeløp(FordelAndelModell arbeidMedOmfordelbartBg);

    private BigDecimal finnSamletBeløpFraArbeidsforhold(Optional<Arbeidsforhold> arbeidsforhold, Function<FordelAndelModell, Optional<BigDecimal>> getBeløpOptional) {
        return modell.getInput().getAlleAndelerForStatus(AktivitetStatus.AT).stream()
            .filter(a -> Objects.equals(a.getArbeidsforhold().orElse(null), arbeidsforhold.orElse(null)))
            .map(getBeløpOptional)
            .filter(Optional::isPresent).map(Optional::get)
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    private void adderBeløpTilRefusjonForArbeidsforhold(FordelAndelModell eksisterende, FordelAndelModell aktivitet, BigDecimal beløpSomSkalOmfordelesTilArbeidsforhold) {
        if (erEksisterende(aktivitet)) {
            return;
        }
        if (eksisterende.getGradertRefusjonPrÅr().isEmpty()) {
            throw new IllegalStateException("Eksisterende andel har ikke refusjonskrav.");
        }
        if (eksisterende.getGradertRefusjonPrÅr().get().compareTo(beløpSomSkalOmfordelesTilArbeidsforhold) < 0) { // NOSONAR
            throw new IllegalStateException("Skal ikke flytte mer av refusjonskravet.");
        }

	    BigDecimal nyRefusjon = eksisterende.getGradertRefusjonPrÅr().get().subtract(beløpSomSkalOmfordelesTilArbeidsforhold); // NOSONAR
	    FordelAndelModell.oppdater(eksisterende)
			    .medFordeltRefusjonPrÅr(skalerOpp(nyRefusjon, eksisterende.getUtbetalingsgrad()))
			    .medGjeldendeRefusjonPrÅr(skalerOpp(nyRefusjon, eksisterende.getUtbetalingsgrad()));

	    BigDecimal fordeltRefusjon = aktivitet.getGradertRefusjonPrÅr().isPresent() ? aktivitet.getGradertRefusjonPrÅr().get().add(beløpSomSkalOmfordelesTilArbeidsforhold) : beløpSomSkalOmfordelesTilArbeidsforhold; // NOSONAR
	    FordelAndelModell.oppdater(aktivitet)
            .medFordeltRefusjonPrÅr(skalerOpp(fordeltRefusjon, aktivitet.getUtbetalingsgrad()))
		    .medGjeldendeRefusjonPrÅr(skalerOpp(fordeltRefusjon, aktivitet.getUtbetalingsgrad()));
    }

    private FordelAndelModell finnEksisterende(FordelPeriodeModell beregningsgrunnlagPeriode, Optional<Arbeidsforhold> arbeidsforhold) {
        return beregningsgrunnlagPeriode.getAlleAndelerForStatus(AktivitetStatus.AT)
            .stream()
            .filter(a -> Objects.equals(a.getArbeidsforhold().orElse(null), arbeidsforhold.orElse(null)) && erEksisterende(a))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Fant ikke eksisterende BeregningsgrunnlagPrArbeidsforhold for " + arbeidsforhold));
    }

    private boolean erEksisterende(FordelAndelModell a) {
        return a.getAndelNr() != null;
    }

    private BigDecimal flyttBGFraAktivitetTilArbeid(FordelAndelModell arbeid,
                                                    BigDecimal restÅFlytte,
                                                    FordelAndelModell arbeidMedFlyttbartGrunnlag,
                                                    BigDecimal beløpSomSkalFlyttes) {
        flyttFraAktivitet(arbeidMedFlyttbartGrunnlag, beløpSomSkalFlyttes);
        omfordelBGTilAktivitet(arbeid, beløpSomSkalFlyttes);
        adderBeløpTilRefusjonForArbeidsforhold(finnEksisterende(modell.getInput(), arbeid.getArbeidsforhold()), arbeid, beløpSomSkalFlyttes);
        restÅFlytte = restÅFlytte.subtract(beløpSomSkalFlyttes);
        return restÅFlytte;
    }

    abstract void flyttFraAktivitet(FordelAndelModell arbeidMedFlyttbartGrunnlag, BigDecimal beløpSomSkalFlyttes);

    private static boolean harMerÅOmfordele(BigDecimal beløpSomMåOmfordeles) {
        return beløpSomMåOmfordeles.compareTo(BigDecimal.ZERO) > 0;
    }

    private static void omfordelBGTilAktivitet(FordelAndelModell aktivitet, BigDecimal beløpSomMåOmfordeles) {
        FordelAndelModell.oppdater(aktivitet).medFordeltPrÅr(skalerOpp(aktivitet.getGradertBruttoPrÅr().orElse(BigDecimal.ZERO).add(beløpSomMåOmfordeles), aktivitet.getUtbetalingsgrad()));
    }

	private static BigDecimal skalerOpp(BigDecimal nyttFordeltBeløp, BigDecimal utbetalingsgrad) {
		return nyttFordeltBeløp.multiply(BigDecimal.valueOf(100).divide(utbetalingsgrad, 10, RoundingMode.HALF_UP));
	}

    private static boolean skalOmfordeleHeleGrunnlaget(BigDecimal restSomMåOmfordeles, BigDecimal omfordelbartBeløp) {
        return restSomMåOmfordeles.compareTo(omfordelbartBeløp) >= 0;
    }

    @FunctionalInterface
    interface FinnArbeidsforholdMedOmfordelbartGrunnlag {
        Optional<FordelAndelModell> finn(FordelPeriodeModell periode);
    }
}
