package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;

class FinnRapportertÅrsinntektSN {

    static BigDecimal finnRapportertÅrsinntekt(BeregningsgrunnlagPeriode grunnlag) {
        Optional<BigDecimal> inntektÅretFørStp = finnInntekterÅretFørStp(grunnlag);
        return inntektÅretFørStp.orElseGet(() -> finnInntekterISammeÅrSomStp(grunnlag).orElse(BigDecimal.ZERO));
    }

    private static Optional<BigDecimal> finnInntekterÅretFørStp(BeregningsgrunnlagPeriode grunnlag) {
	    var skjæringstidspunkt = grunnlag.getBeregningsgrunnlag().getSkjæringstidspunkt();
	    var åretFørStp = skjæringstidspunkt.minusYears(1);
	    var førsteDagIÅretFørStp = åretFørStp.withDayOfYear(1);
	    List<Periodeinntekt> inntekterÅretFørStp = grunnlag.getInntektsgrunnlag().getPeriodeinntekterForSNFraSøknad(Periode.of(førsteDagIÅretFørStp, åretFørStp.with(TemporalAdjusters.lastDayOfYear())));
        if (inntekterÅretFørStp.isEmpty()) {
            return Optional.empty();
        }
        LocalDate førsteDatoMedInntekt = inntekterÅretFørStp.stream().map(Periodeinntekt::getFom).min(Comparator.naturalOrder()).orElse(skjæringstidspunkt.withDayOfYear(1));
        Optional<BigDecimal> sumIPeriode = inntekterÅretFørStp.stream().map(Periodeinntekt::getInntekt).reduce(BigDecimal::add);
        if (førsteDatoMedInntekt.isAfter(førsteDagIÅretFørStp)) {
            int virkedager = Virkedager.beregnAntallVirkedagerEllerKunHelg(førsteDatoMedInntekt, skjæringstidspunkt.withDayOfYear(1));
            if (virkedager > 0) {
                Optional<BigDecimal> dagsats = sumIPeriode.map(b -> b.divide(BigDecimal.valueOf(virkedager), 10, RoundingMode.HALF_EVEN));
                return dagsats.map(b -> b.multiply(BigDecimal.valueOf(260)));
            }
        }
        return sumIPeriode;
    }

    private static Optional<BigDecimal> finnInntekterISammeÅrSomStp(BeregningsgrunnlagPeriode grunnlag) {
        LocalDate førsteDatoMedInntekt;
	    var skjæringstidspunkt = grunnlag.getBeregningsgrunnlag().getSkjæringstidspunkt();
	    List<Periodeinntekt> inntekterSammeÅrSomStp = grunnlag.getInntektsgrunnlag().getPeriodeinntekterForSNFraSøknad(Periode.of(skjæringstidspunkt.withDayOfYear(1), skjæringstidspunkt));
        førsteDatoMedInntekt = inntekterSammeÅrSomStp.stream().map(Periodeinntekt::getFom).min(Comparator.naturalOrder()).orElse(skjæringstidspunkt.withDayOfYear(1));
        Optional<BigDecimal> sumIPeriode = inntekterSammeÅrSomStp.stream().map(Periodeinntekt::getInntekt).reduce(BigDecimal::add);
        int virkedager = Virkedager.beregnAntallVirkedagerEllerKunHelg(førsteDatoMedInntekt, skjæringstidspunkt);
        if (virkedager > 0) {
            Optional<BigDecimal> dagsats = sumIPeriode.map(b -> b.divide(BigDecimal.valueOf(virkedager), 10, RoundingMode.HALF_EVEN));
            return dagsats.map(b -> b.multiply(BigDecimal.valueOf(260)));
        }
        return Optional.empty();
    }

}
