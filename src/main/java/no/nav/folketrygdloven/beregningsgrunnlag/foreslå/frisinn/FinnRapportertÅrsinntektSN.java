package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;

class FinnRapportertÅrsinntektSN {

	private FinnRapportertÅrsinntektSN() {
		// Skjuler default konstruktør
	}

    static BigDecimal finnRapportertÅrsinntekt(BeregningsgrunnlagPeriode grunnlag) {
        Optional<BigDecimal> inntekt2019 = finnInntekter2019(grunnlag);
        return inntekt2019.orElseGet(() -> finnInntekter2020(grunnlag).orElse(BigDecimal.ZERO));
    }

    private static Optional<BigDecimal> finnInntekter2019(BeregningsgrunnlagPeriode grunnlag) {
        List<Periodeinntekt> inntekter2019 = grunnlag.getInntektsgrunnlag().getPeriodeinntekterForSNFraSøknad(Periode.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 12, 31)));
        if (inntekter2019.isEmpty()) {
            return Optional.empty();
        }
        LocalDate førsteDatoMedInntekt = inntekter2019.stream().map(Periodeinntekt::getFom).min(Comparator.naturalOrder()).orElse(LocalDate.of(2020, 1, 1));
        Optional<BigDecimal> sumIPeriode = inntekter2019.stream().map(Periodeinntekt::getInntekt).reduce(BigDecimal::add);
        if (førsteDatoMedInntekt.isAfter(LocalDate.of(2019,1, 1))) {
            int virkedager = Virkedager.beregnAntallVirkedagerEllerKunHelg(førsteDatoMedInntekt, LocalDate.of(2020, 1, 1));
            if (virkedager > 0) {
                Optional<BigDecimal> dagsats = sumIPeriode.map(b -> b.divide(BigDecimal.valueOf(virkedager), 10, RoundingMode.HALF_EVEN));
                return dagsats.map(b -> b.multiply(BigDecimal.valueOf(260)));
            }
        }
        return sumIPeriode;
    }

    private static Optional<BigDecimal> finnInntekter2020(BeregningsgrunnlagPeriode grunnlag) {
        LocalDate førsteDatoMedInntekt;
        List<Periodeinntekt> inntekter2020 = grunnlag.getInntektsgrunnlag().getPeriodeinntekterForSNFraSøknad(Periode.of(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 3, 1)));
        førsteDatoMedInntekt = inntekter2020.stream().map(Periodeinntekt::getFom).min(Comparator.naturalOrder()).orElse(LocalDate.of(2020, 1, 1));
        Optional<BigDecimal> sumIPeriode = inntekter2020.stream().map(Periodeinntekt::getInntekt).reduce(BigDecimal::add);
        int virkedager = Virkedager.beregnAntallVirkedagerEllerKunHelg(førsteDatoMedInntekt, LocalDate.of(2020, 3, 1));
        if (virkedager > 0) {
            Optional<BigDecimal> dagsats = sumIPeriode.map(b -> b.divide(BigDecimal.valueOf(virkedager), 10, RoundingMode.HALF_EVEN));
            return dagsats.map(b -> b.multiply(BigDecimal.valueOf(260)));
        }
        return Optional.empty();
    }

}
