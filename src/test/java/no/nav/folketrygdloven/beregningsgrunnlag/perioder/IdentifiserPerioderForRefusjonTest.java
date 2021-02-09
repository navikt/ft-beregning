package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.RefusjonskravFrist;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
import no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil;

public class IdentifiserPerioderForRefusjonTest {

	private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2019, Month.JANUARY, 4);

	@Test
	public void ingenRefusjon() {
		// Arrange
		var im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(null)
				.medRefusjonskrav(List.of())
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, new HashMap<>());

		// Assert
		assertThat(periodesplitter).isEmpty();
	}

	@Test
	public void refusjonFraSkjæringstidspunkt() {
		// Arrange
		var im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(LocalDate.of(2019, Month.APRIL, 30))
				.medRefusjonskrav(List.of(new Refusjonskrav(BigDecimal.TEN, SKJÆRINGSTIDSPUNKT, DateUtil.TIDENES_ENDE)))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(1);
		PeriodeSplittData periodeSplitt = periodesplitter.iterator().next();
		assertPeriodeSplitt(periodeSplitt, im, SKJÆRINGSTIDSPUNKT, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10);
	}

	/**
	 * Se {@link MapRefusjonskravFraVLTilRegelTest#refusjonFraSenereDato}
	 */
	@Test
	public void refusjonFraSenereDato() {
		// Arrange
		LocalDate endringFom = LocalDate.of(2019, Month.JANUARY, 26);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(LocalDate.of(2019, Month.APRIL, 30))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.ZERO, SKJÆRINGSTIDSPUNKT, endringFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.TEN, endringFom, DateUtil.TIDENES_ENDE)
				))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(1);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, endringFom, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10)
		);
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
	public void refusjonFraStartOgOpphør() {
		// Arrange
		LocalDate opphørFom = LocalDate.of(2019, Month.JANUARY, 26);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(LocalDate.of(2019, Month.APRIL, 30))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.TEN, SKJÆRINGSTIDSPUNKT, LocalDate.of(2019, Month.JANUARY, 25)),
						new Refusjonskrav(BigDecimal.ZERO, opphørFom, DateUtil.TIDENES_ENDE)
				))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, new HashMap<>());

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
	public void refusjonFraStartOgEndring() {
		// Arrange
		LocalDate endringFom = LocalDate.of(2019, Month.JANUARY, 26);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(LocalDate.of(2019, Month.APRIL, 30))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.valueOf(20000), SKJÆRINGSTIDSPUNKT, LocalDate.of(2019, Month.JANUARY, 25)),
						new Refusjonskrav(BigDecimal.valueOf(10000), endringFom, DateUtil.TIDENES_ENDE)
				))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, new HashMap<>());

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
	public void refusjonFraStartEndringOgOpphør() {
		// Arrange
		LocalDate endringFom = LocalDate.of(2019, Month.JANUARY, 26);
		LocalDate opphørFom = LocalDate.of(2019, Month.FEBRUARY, 17);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(LocalDate.of(2019, Month.APRIL, 30))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.valueOf(20000), SKJÆRINGSTIDSPUNKT, LocalDate.of(2019, Month.JANUARY, 25)),
						new Refusjonskrav(BigDecimal.valueOf(10000), endringFom, opphørFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.ZERO, opphørFom, DateUtil.TIDENES_ENDE)
				))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, new HashMap<>());

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
	public void refusjonFraSkjæringstidspunktArbeidsgiverSøkerForSent() {
		// Arrange
		var im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(LocalDate.of(2019, Month.MAY, 1))
				.medRefusjonskravFrist(new RefusjonskravFrist(3, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST))
				.medRefusjonskrav(List.of(new Refusjonskrav(BigDecimal.TEN, SKJÆRINGSTIDSPUNKT, DateUtil.TIDENES_ENDE)))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(1);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, LocalDate.of(2019, Month.FEBRUARY, 1), PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10)
		);
	}

	@Test
	public void refusjonFraStartOgOpphørArbeidsgiverSøkerForSent() {
		// Arrange
		LocalDate opphørFom = LocalDate.of(2019, Month.JANUARY, 26);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(LocalDate.of(2019, Month.MAY, 2))
				.medRefusjonskravFrist(new RefusjonskravFrist(3, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.TEN, SKJÆRINGSTIDSPUNKT, opphørFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.ZERO, opphørFom, DateUtil.TIDENES_ENDE)
				))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, new HashMap<>());

		// Assert
		assertThat(periodesplitter).isEmpty();
	}

	// Første inntektsmelding med refusjonskrav: 2. mai
	// Bedt om refusjon fra 4. januar, opphør 15. mars
	// Forventet resultat: Får refusjon fra 1. februar, opphør fra 15. mars
	@Test
	public void refusjonFraStartOgOpphørEtterFørsteDagIMånedenFørsteIMMottattMinus3MånederArbeidsgiverSøkerForSent() {
		// Arrange
		LocalDate opphørFom = LocalDate.of(2019, Month.MARCH, 15);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(LocalDate.of(2019, Month.MAY, 2))
				.medRefusjonskravFrist(new RefusjonskravFrist(3, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.TEN, SKJÆRINGSTIDSPUNKT, opphørFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.ZERO, opphørFom, DateUtil.TIDENES_ENDE)
				))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(2);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, LocalDate.of(2019, Month.FEBRUARY, 1), PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10)
		);
		assertThat(periodesplitter).anySatisfy(opphør ->
				assertPeriodeSplitt(opphør, im, opphørFom, PeriodeÅrsak.REFUSJON_OPPHØRER, 0)
		);
	}

	@Test
	public void refusjonFraStartOgEndringArbeidsgiverSøkerForSent() {
		// Arrange
		LocalDate endringFom = LocalDate.of(2019, Month.JANUARY, 26);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(LocalDate.of(2019, Month.MAY, 14))
				.medRefusjonskravFrist(new RefusjonskravFrist(3, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.valueOf(20000), SKJÆRINGSTIDSPUNKT, LocalDate.of(2019, Month.JANUARY, 25)),
						new Refusjonskrav(BigDecimal.valueOf(10000), endringFom, DateUtil.TIDENES_ENDE)
				))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(1);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, LocalDate.of(2019, Month.FEBRUARY, 1), PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10_000)
		);
	}

	@Test
	public void refusjonFraStartOgEndringEtter1FebruarArbeidsgiverSøkerForSent() {
		// Arrange
		LocalDate endringFom = LocalDate.of(2019, Month.FEBRUARY, 26);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(LocalDate.of(2019, Month.MAY, 14))
				.medRefusjonskravFrist(new RefusjonskravFrist(3, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.valueOf(20000), SKJÆRINGSTIDSPUNKT, endringFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.valueOf(10000), endringFom, DateUtil.TIDENES_ENDE)
				))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(2);
		assertThat(periodesplitter).anySatisfy(endring1 ->
				assertPeriodeSplitt(endring1, im, LocalDate.of(2019, Month.FEBRUARY, 1), PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 20_000)
		);
		assertThat(periodesplitter).anySatisfy(endring2 ->
				assertPeriodeSplitt(endring2, im, endringFom, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10_000)
		);
	}

	@Test
	public void refusjonFraStartEndringOgOpphørArbeidsgiverSøkerForSent() {
		// Arrange
		LocalDate endringFom = LocalDate.of(2019, Month.JANUARY, 26);
		LocalDate opphørFom = LocalDate.of(2019, Month.FEBRUARY, 17);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(LocalDate.of(2019, Month.MAY, 31))
				.medRefusjonskravFrist(new RefusjonskravFrist(3, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.valueOf(20000), SKJÆRINGSTIDSPUNKT, endringFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.valueOf(10000), endringFom, opphørFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.ZERO, opphørFom, DateUtil.TIDENES_ENDE)
				))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(2);
		assertThat(periodesplitter).anySatisfy(endring ->
				assertPeriodeSplitt(endring, im, LocalDate.of(2019, Month.FEBRUARY, 1), PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10_000)
		);
		assertThat(periodesplitter).anySatisfy(opphør ->
				assertPeriodeSplitt(opphør, im, opphørFom, PeriodeÅrsak.REFUSJON_OPPHØRER, 0)
		);
	}

	@Test
	public void refusjonFraStartEndringEtter1FebruarOgOpphørArbeidsgiverSøkerForSent() {
		// Arrange
		LocalDate endringFom = LocalDate.of(2019, Month.FEBRUARY, 26);
		LocalDate opphørFom = LocalDate.of(2019, Month.MARCH, 17);
		ArbeidsforholdOgInntektsmelding im = ArbeidsforholdOgInntektsmelding.builder()
				.medStartdatoPermisjon(SKJÆRINGSTIDSPUNKT)
				.medInnsendingsdatoFørsteInntektsmeldingMedRefusjon(LocalDate.of(2019, Month.MAY, 31))
				.medRefusjonskravFrist(new RefusjonskravFrist(3, BeregningsgrunnlagHjemmel.REFUSJONSKRAV_FRIST))
				.medRefusjonskrav(List.of(
						new Refusjonskrav(BigDecimal.valueOf(20000), SKJÆRINGSTIDSPUNKT, endringFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.valueOf(10000), endringFom, opphørFom.minusDays(1)),
						new Refusjonskrav(BigDecimal.ZERO, opphørFom, DateUtil.TIDENES_ENDE)
				))
				.build();

		// Act
		Set<PeriodeSplittData> periodesplitter = IdentifiserPerioderForRefusjon.identifiserPerioderForRefusjon(im, new HashMap<>());

		// Assert
		assertThat(periodesplitter).hasSize(3);
		assertThat(periodesplitter).anySatisfy(endring1 ->
				assertPeriodeSplitt(endring1, im, LocalDate.of(2019, Month.FEBRUARY, 1), PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 20_000)
		);
		assertThat(periodesplitter).anySatisfy(endring2 ->
				assertPeriodeSplitt(endring2, im, endringFom, PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, 10_000)
		);
		assertThat(periodesplitter).anySatisfy(opphør ->
				assertPeriodeSplitt(opphør, im, opphørFom, PeriodeÅrsak.REFUSJON_OPPHØRER, 0)
		);
	}
}
