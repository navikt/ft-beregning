package no.nav.folketrygdloven.beregningsgrunnlag.perioder.utbetalingsgrad;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.AndelUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.Utbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public class IdentifiserPerioderForEndringISøktYtelse {
    private IdentifiserPerioderForEndringISøktYtelse() {
        // skjul public constructor
    }

    public static Set<PeriodeSplittData> identifiser(AndelUtbetalingsgrad endringISøktYtelse) {
        List<Utbetalingsgrad> graderinger = endringISøktYtelse.getUbetalingsgrader();
	    var graderingTidslinje = lagGraderingTidslinje(graderinger);
	    if (graderingTidslinje.isEmpty()) {
		    return Collections.emptySet();
	    }
	    var tidslinje = fyllMellomromMedNull(graderingTidslinje);
	    var set = tidslinje.getLocalDateIntervals().stream()
			    .map(graderingIntervall -> lagPeriodeSplitt(graderingIntervall.getFomDato()))
			    .collect(Collectors.toCollection(HashSet::new));
        return set.stream().sorted(Comparator.comparing(PeriodeSplittData::getFom)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

	private static LocalDateTimeline<BigDecimal> lagGraderingTidslinje(List<Utbetalingsgrad> graderinger) {
		var graderingSegmenter = graderinger.stream()
				.map(g -> new LocalDateSegment<>(g.getFom(), g.getTom(), g.getUtbetalingsprosent()))
				.collect(Collectors.toList());
		return new LocalDateTimeline<>(graderingSegmenter, StandardCombinators::rightOnly);
	}

	private static LocalDateTimeline<BigDecimal> fyllMellomromMedNull(LocalDateTimeline<BigDecimal> graderingTidslinje) {
		var førsteFom = graderingTidslinje.getLocalDateIntervals().stream().map(LocalDateInterval::getFomDato).min(Comparator.naturalOrder()).orElseThrow();
		var nullTidslinje = new LocalDateTimeline<>(List.of(new LocalDateSegment<>(førsteFom, TIDENES_ENDE, BigDecimal.ZERO)));
		return graderingTidslinje.crossJoin(nullTidslinje, StandardCombinators::coalesceLeftHandSide).compress((v1, v2) -> v1.compareTo(v2) == 0, (i, lhs, rhs) -> new LocalDateSegment<>(i, lhs.getValue()));
	}

	private static PeriodeSplittData lagPeriodeSplitt(LocalDate fom) {
        return PeriodeSplittData.builder()
            .medPeriodeÅrsak(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR)
            .medFom(fom)
            .build();
    }
}
