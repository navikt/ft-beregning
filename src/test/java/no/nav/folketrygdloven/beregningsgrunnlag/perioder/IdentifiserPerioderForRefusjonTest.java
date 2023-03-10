package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.perioder.refusjon.IdentifiserPerioderForRefusjon;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.RefusjonskravFrist;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Utfall;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public class IdentifiserPerioderForRefusjonTest {

	private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, Month.JANUARY, 4);

	@Test
	void ingenRefusjon() {
		// Arrange
		var im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medRefusjonskrav(List.of())
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(
				im,
				new LocalDateTimeline<>(Collections.emptyList()), new HashMap<>());

		// Assert
		assertThat(periodesplitter).isEmpty();
	}

	@Test
	void refusjonFraSkjæringstidspunkt() {
		// Arrange
		var im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medRefusjonskrav(List.of(new Refusjonskrav(BigDecimal.TEN, SKJÆRINGSTIDSPUNKT, TIDENES_ENDE)))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, godkjentFra(SKJÆRINGSTIDSPUNKT), new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(1);
		PeriodeSplittData periodeSplitt = periodesplitter.iterator().next();
		assertPeriodeSplitt(periodeSplitt, im, SKJÆRINGSTIDSPUNKT, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10);
	}

	private LocalDateTimeline<Utfall> godkjentFra(LocalDate fra) {
		return godkjennIPerioder(List.of(Periode.of(fra, TIDENES_ENDE)));
	}

	private LocalDateTimeline<Utfall> godkjennIPerioder(List<Periode> perioder) {
		return new LocalDateTimeline<>(perioder.stream().map(p -> new LocalDateSegment<>(p.getFom(), p.getTom(), Utfall.GODKJENT)).collect(Collectors.toList()));
	}

	private LocalDateTimeline<Utfall> avslåIPerioder(List<Periode> perioder) {
		return new LocalDateTimeline<>(perioder.stream().map(p -> new LocalDateSegment<>(p.getFom(), p.getTom(), Utfall.UNDERKJENT)).collect(Collectors.toList()));
	}


	@Test
	void refusjonFraSenereDato() {
		// Arrange
		LocalDate endringFom = LocalDate.of(2019, Month.JANUARY, 26);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.ZERO, SKJÆRINGSTIDSPUNKT, endringFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.TEN, endringFom, TIDENES_ENDE)
				))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im,
				godkjennIAllePerioder(im.getRefusjoner()),
				new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(1);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, endringFom, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10)
		);
	}

	private LocalDateTimeline<Utfall> godkjennIAllePerioder(List<Refusjonskrav> refusjoner) {
		return godkjennIPerioder(refusjoner.stream().map(Refusjonskrav::getPeriode).collect(Collectors.toList()));
	}

	private LocalDateTimeline<Utfall> avslåttIAllePerioder(List<Refusjonskrav> refusjoner) {
		return avslåIPerioder(refusjoner.stream().map(Refusjonskrav::getPeriode).collect(Collectors.toList()));
	}

	private LocalDateTimeline<Utfall> tidslinjeMedAvslåttOgGodkjent(List<Periode> godkjentPerioder, List<Periode> avslåttPerioder) {
		LocalDateTimeline<Utfall> godkjentTimeline = godkjennIPerioder(godkjentPerioder);
		LocalDateTimeline<Utfall> avslåttTimeline = avslåIPerioder(avslåttPerioder);
		return godkjentTimeline.combine(avslåttTimeline, StandardCombinators::coalesceLeftHandSide, LocalDateTimeline.JoinStyle.CROSS_JOIN);
	}

	private void assertPeriodeSplitt(PeriodeSplittData periodeSplitt, ArbeidsforholdOgInntektsmelding im,
	                                 LocalDate fom,
	                                 PeriodeÅrsak periodeÅrsak,
	                                 int refusjonskravPrMåned) {
		assertThat(periodeSplitt.getFom()).isEqualTo(fom);
		assertThat(periodeSplitt.getPeriodeÅrsak()).isEqualTo(periodeÅrsak);
		assertThat(periodeSplitt.getRefusjonskravPrMåned()).isEqualTo(BigDecimal.valueOf(refusjonskravPrMåned));
		assertThat(periodeSplitt.getInntektsmelding()).isEqualTo(im);
	}

	@Test
	void refusjonFraStartOgOpphør() {
		// Arrange
		LocalDate opphørFom = LocalDate.of(2019, Month.JANUARY, 26);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.TEN, SKJÆRINGSTIDSPUNKT, LocalDate.of(2019, Month.JANUARY, 25)),
						new Refusjonskrav(BigDecimal.ZERO, opphørFom, TIDENES_ENDE)
				))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im,
				godkjennIAllePerioder(im.getRefusjoner()), new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(2);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, SKJÆRINGSTIDSPUNKT, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10)
		);
		assertThat(periodesplitter).anySatisfy(opphør ->
				assertPeriodeSplitt(opphør, im, opphørFom, PeriodeÅrsak.REFUSJON_OPPHØRER, 0)
		);
	}

	@Test
	void refusjonFraStartOgEndring() {
		// Arrange
		LocalDate endringFom = LocalDate.of(2019, Month.JANUARY, 26);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.valueOf(20000), SKJÆRINGSTIDSPUNKT, LocalDate.of(2019, Month.JANUARY, 25)),
						new Refusjonskrav(BigDecimal.valueOf(10000), endringFom, TIDENES_ENDE)
				))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, godkjennIAllePerioder(im.getRefusjoner()), new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(2);
		assertThat(periodesplitter).anySatisfy(start ->
				assertPeriodeSplitt(start, im, SKJÆRINGSTIDSPUNKT, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 20_000)
		);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, endringFom, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10_000)
		);
	}

	@Test
	void refusjonFraStartEndringOgOpphør() {
		// Arrange
		LocalDate endringFom = LocalDate.of(2019, Month.JANUARY, 26);
		LocalDate opphørFom = LocalDate.of(2019, Month.FEBRUARY, 17);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.valueOf(20000), SKJÆRINGSTIDSPUNKT, LocalDate.of(2019, Month.JANUARY, 25)),
						new Refusjonskrav(BigDecimal.valueOf(10000), endringFom, opphørFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.ZERO, opphørFom, TIDENES_ENDE)
				))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, godkjennIAllePerioder(im.getRefusjoner()), new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(3);
		assertThat(periodesplitter).anySatisfy(start ->
				assertPeriodeSplitt(start, im, SKJÆRINGSTIDSPUNKT, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 20_000)
		);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, endringFom, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10_000)
		);
		assertThat(periodesplitter).anySatisfy(opphør ->
				assertPeriodeSplitt(opphør, im, opphørFom, PeriodeÅrsak.REFUSJON_OPPHØRER, 0)
		);
	}

	@Test
	void refusjonFraSkjæringstidspunktArbeidsgiverSøkerForSent() {
		// Arrange
		var im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medRefusjonskravFrist(new RefusjonskravFrist(3, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST))
				.medRefusjonskrav(List.of(new Refusjonskrav(BigDecimal.TEN, SKJÆRINGSTIDSPUNKT, TIDENES_ENDE)))
				.build();
		LocalDate godkjentRefusjonFom = LocalDate.of(2019, Month.FEBRUARY, 1);

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im,
				tidslinjeMedAvslåttOgGodkjent(List.of(Periode.of(godkjentRefusjonFom, TIDENES_ENDE)),
						List.of(Periode.of(SKJÆRINGSTIDSPUNKT, godkjentRefusjonFom.minusDays(1)))), new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(2);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, SKJÆRINGSTIDSPUNKT, PeriodeÅrsak.REFUSJON_AVSLÅTT, 10)
		);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, godkjentRefusjonFom, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10)
		);
	}

	@Test
	void refusjonFraStartOgOpphørArbeidsgiverSøkerForSent() {
		// Arrange
		LocalDate opphørFom = LocalDate.of(2019, Month.JANUARY, 26);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medRefusjonskravFrist(new RefusjonskravFrist(3, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.TEN, SKJÆRINGSTIDSPUNKT, opphørFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.ZERO, opphørFom, TIDENES_ENDE)
				))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im,
				avslåttIAllePerioder(im.getRefusjoner()), new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(2);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, SKJÆRINGSTIDSPUNKT, PeriodeÅrsak.REFUSJON_AVSLÅTT, 10)
		);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, opphørFom, PeriodeÅrsak.REFUSJON_AVSLÅTT, 0)
		);
	}

	// Første inntektsmelding med refusjonskrav: 2. mai
	// Bedt om refusjon fra 4. januar, opphør 15. mars
	// Forventet resultat: Får refusjon fra 1. februar, opphør fra 15. mars
	@Test
	void refusjonFraStartOgOpphørEtterFørsteDagIMånedenFørsteIMMottattMinus3MånederArbeidsgiverSøkerForSent() {
		// Arrange
		LocalDate opphørFom = LocalDate.of(2019, Month.MARCH, 15);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medRefusjonskravFrist(new RefusjonskravFrist(3, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.TEN, SKJÆRINGSTIDSPUNKT, opphørFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.ZERO, opphørFom, TIDENES_ENDE)
				))
				.build();
		LocalDate refusjonGodkjentFom = LocalDate.of(2019, Month.FEBRUARY, 1);

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im,
				tidslinjeMedAvslåttOgGodkjent(
						List.of(Periode.of(refusjonGodkjentFom, TIDENES_ENDE)),
						List.of(Periode.of(SKJÆRINGSTIDSPUNKT, refusjonGodkjentFom.minusDays(1)))),
				new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(3);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, SKJÆRINGSTIDSPUNKT, PeriodeÅrsak.REFUSJON_AVSLÅTT, 10)
		);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, refusjonGodkjentFom, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10)
		);
		assertThat(periodesplitter).anySatisfy(opphør ->
				assertPeriodeSplitt(opphør, im, opphørFom, PeriodeÅrsak.REFUSJON_OPPHØRER, 0)
		);
	}

	@Test
	void refusjonFraStartOgEndringArbeidsgiverSøkerForSent() {
		// Arrange
		LocalDate endringFom = LocalDate.of(2019, Month.JANUARY, 26);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medRefusjonskravFrist(new RefusjonskravFrist(3, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.valueOf(20000), SKJÆRINGSTIDSPUNKT, LocalDate.of(2019, Month.JANUARY, 25)),
						new Refusjonskrav(BigDecimal.valueOf(10000), endringFom, TIDENES_ENDE)
				))
				.build();
		LocalDate refusjonGodkjentFom = LocalDate.of(2019, Month.FEBRUARY, 1);

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im,
				tidslinjeMedAvslåttOgGodkjent(
						List.of(Periode.of(refusjonGodkjentFom, TIDENES_ENDE)),
						List.of(Periode.of(SKJÆRINGSTIDSPUNKT, refusjonGodkjentFom.minusDays(1)))),
				new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(3);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, SKJÆRINGSTIDSPUNKT, PeriodeÅrsak.REFUSJON_AVSLÅTT, 20000)
		);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, endringFom, PeriodeÅrsak.REFUSJON_AVSLÅTT, 10_000)
		);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, refusjonGodkjentFom, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10_000)
		);
	}

	@Test
	void refusjonFraStartOgEndringEtter1FebruarArbeidsgiverSøkerForSent() {
		// Arrange
		LocalDate endringFom = LocalDate.of(2019, Month.FEBRUARY, 26);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medRefusjonskravFrist(new RefusjonskravFrist(3, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.valueOf(20000), SKJÆRINGSTIDSPUNKT, endringFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.valueOf(10000), endringFom, TIDENES_ENDE)
				))
				.build();
		LocalDate refusjonGodkjentFom = LocalDate.of(2019, Month.FEBRUARY, 1);

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im,
				tidslinjeMedAvslåttOgGodkjent(
						List.of(Periode.of(refusjonGodkjentFom, TIDENES_ENDE)),
						List.of(Periode.of(SKJÆRINGSTIDSPUNKT, refusjonGodkjentFom.minusDays(1)))),
				new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(3);
		assertThat(periodesplitter).anySatisfy(endring1 ->
				assertPeriodeSplitt(endring1, im, SKJÆRINGSTIDSPUNKT, PeriodeÅrsak.REFUSJON_AVSLÅTT, 20_000)
		);
		assertThat(periodesplitter).anySatisfy(endring1 ->
				assertPeriodeSplitt(endring1, im, refusjonGodkjentFom, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 20_000)
		);
		assertThat(periodesplitter).anySatisfy(endring2 ->
				assertPeriodeSplitt(endring2, im, endringFom, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10_000)
		);
	}

	@Test
	void refusjonFraStartEndringOgOpphørArbeidsgiverSøkerForSent() {
		// Arrange
		LocalDate endringFom = LocalDate.of(2019, Month.JANUARY, 26);
		LocalDate opphørFom = LocalDate.of(2019, Month.FEBRUARY, 17);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medRefusjonskravFrist(new RefusjonskravFrist(3, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.valueOf(20000), SKJÆRINGSTIDSPUNKT, endringFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.valueOf(10000), endringFom, opphørFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.ZERO, opphørFom, TIDENES_ENDE)
				))
				.build();
		LocalDate godkjentRefusjonFom = LocalDate.of(2019, Month.FEBRUARY, 1);

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im,
				tidslinjeMedAvslåttOgGodkjent(
						List.of(Periode.of(godkjentRefusjonFom, TIDENES_ENDE)),
						List.of(Periode.of(SKJÆRINGSTIDSPUNKT, godkjentRefusjonFom.minusDays(1)))),
				new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(4);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, SKJÆRINGSTIDSPUNKT, PeriodeÅrsak.REFUSJON_AVSLÅTT, 20000)
		);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, endringFom, PeriodeÅrsak.REFUSJON_AVSLÅTT, 10_000)
		);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, godkjentRefusjonFom, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10_000)
		);
		assertThat(periodesplitter).anySatisfy(opphør ->
				assertPeriodeSplitt(opphør, im, opphørFom, PeriodeÅrsak.REFUSJON_OPPHØRER, 0)
		);
	}

	@Test
	void refusjonFraStartEndringEtter1FebruarOgOpphørArbeidsgiverSøkerForSent() {
		// Arrange
		LocalDate endringFom = LocalDate.of(2019, Month.FEBRUARY, 26);
		LocalDate opphørFom = LocalDate.of(2019, Month.MARCH, 17);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medRefusjonskravFrist(new RefusjonskravFrist(3, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.valueOf(20000), SKJÆRINGSTIDSPUNKT, endringFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.valueOf(10000), endringFom, opphørFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.ZERO, opphørFom, TIDENES_ENDE)
				))
				.build();
		LocalDate godkjentRefusjonFom = LocalDate.of(2019, Month.FEBRUARY, 1);

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im,
				tidslinjeMedAvslåttOgGodkjent(
				List.of(Periode.of(godkjentRefusjonFom, TIDENES_ENDE)),
				List.of(Periode.of(SKJÆRINGSTIDSPUNKT, godkjentRefusjonFom.minusDays(1)))),
				new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(4);
		assertThat(periodesplitter).anySatisfy(endring1 ->
				assertPeriodeSplitt(endring1, im, SKJÆRINGSTIDSPUNKT, PeriodeÅrsak.REFUSJON_AVSLÅTT, 20_000)
		);
		assertThat(periodesplitter).anySatisfy(endring1 ->
				assertPeriodeSplitt(endring1, im, godkjentRefusjonFom, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 20_000)
		);
		assertThat(periodesplitter).anySatisfy(endring2 ->
				assertPeriodeSplitt(endring2, im, endringFom, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10_000)
		);
		assertThat(periodesplitter).anySatisfy(opphør ->
				assertPeriodeSplitt(opphør, im, opphørFom, PeriodeÅrsak.REFUSJON_OPPHØRER, 0)
		);
	}
}
