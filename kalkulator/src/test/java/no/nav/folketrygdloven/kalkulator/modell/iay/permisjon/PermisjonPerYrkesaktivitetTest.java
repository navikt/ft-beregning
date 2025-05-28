package no.nav.folketrygdloven.kalkulator.modell.iay.permisjon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDtoBuilder;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

class PermisjonPerYrkesaktivitetTest {

	@Test
	void test_foreldrepermisjon_i_over_14_dager_før_skjæringstidspunktet() {
		// Arrange
        var startForeldrepermisjon = LocalDate.of(2023, 1, 1);
        var sluttForeldrepermisjon = LocalDate.of(2023, 1, 20);
		var foreldrepermisjon = lagForeldrepermisjon(startForeldrepermisjon, sluttForeldrepermisjon);
		var yrkesaktivitet = lagYrkesaktivitet(foreldrepermisjon);

		var skjæringstidspunkt = LocalDate.of(2023, 1, 21);

		// Act
        var permisjonTidslinje = PermisjonPerYrkesaktivitet.utledPermisjonPerYrkesaktivitet(yrkesaktivitet, Map.of(), skjæringstidspunkt);

		// Assert
		assertFalse(permisjonTidslinje.isEmpty());
		var expectedTimeline = new LocalDateTimeline<>(startForeldrepermisjon, sluttForeldrepermisjon, true);
		assertEquals(expectedTimeline, permisjonTidslinje);
	}

	@Test
	void test_foreldrepermisjon_i_over_14_dager_med_deler_av_permisjonen_før_skjæringstidspunktet() {
		// Arrange
        var startForeldrepermisjon = LocalDate.of(2023, 1, 1);
        var sluttForeldrepermisjon = LocalDate.of(2023, 1, 20);
		var foreldrepermisjon = lagForeldrepermisjon(startForeldrepermisjon, sluttForeldrepermisjon);
		var yrkesaktivitet = lagYrkesaktivitet(foreldrepermisjon);

		var skjæringstidspunkt = LocalDate.of(2023, 1, 10);

		// Act
        var permisjonTidslinje = PermisjonPerYrkesaktivitet.utledPermisjonPerYrkesaktivitet(yrkesaktivitet, Map.of(), skjæringstidspunkt);

		// Assert
		assertFalse(permisjonTidslinje.isEmpty());
		var expectedTimeline = new LocalDateTimeline<>(startForeldrepermisjon, skjæringstidspunkt.minusDays(1), true);
		assertEquals(expectedTimeline, permisjonTidslinje);
	}

	@Test
	void foreldrepermisjon_som_overlapper_foreldrepenger_skal_ikke_tas_med() {
		// Arrange
        var startForeldrepermisjon = LocalDate.of(2023, 1, 1);
        var sluttForeldrepermisjon = LocalDate.of(2023, 1, 20);
		var foreldrepermisjon = lagForeldrepermisjon(startForeldrepermisjon, sluttForeldrepermisjon);
		var yrkesaktivitet = lagYrkesaktivitet(foreldrepermisjon);
		final var foreldrepengerTidslinje = new LocalDateTimeline<Boolean>(startForeldrepermisjon, sluttForeldrepermisjon, true);
		var skjæringstidspunkt = LocalDate.of(2023, 1, 21);

		// Act
        var permisjonTidslinje = PermisjonPerYrkesaktivitet.utledPermisjonPerYrkesaktivitet(yrkesaktivitet, Map.of(YtelseType.FORELDREPENGER, foreldrepengerTidslinje), skjæringstidspunkt);

		// Assert
		assertTrue(permisjonTidslinje.isEmpty());
	}

	@Test
	void foreldrepermisjon_som_overlapper_svangerskapspenger_skal_ikke_tas_med() {
		// Arrange
        var startForeldrepermisjon = LocalDate.of(2023, 1, 1);
        var sluttForeldrepermisjon = LocalDate.of(2023, 1, 20);
		var foreldrepermisjon = lagForeldrepermisjon(startForeldrepermisjon, sluttForeldrepermisjon);
		var yrkesaktivitet = lagYrkesaktivitet(foreldrepermisjon);
		final var svangerskapspengerTidslinje = new LocalDateTimeline<Boolean>(startForeldrepermisjon, sluttForeldrepermisjon, true);
		var skjæringstidspunkt = LocalDate.of(2023, 1, 21);

		// Act
        var permisjonTidslinje = PermisjonPerYrkesaktivitet.utledPermisjonPerYrkesaktivitet(yrkesaktivitet, Map.of(YtelseType.SVANGERSKAPSPENGER, svangerskapspengerTidslinje), skjæringstidspunkt);

		// Assert
		assertTrue(permisjonTidslinje.isEmpty());
	}

	@Test
	void foreldrepermisjon_som_overlapper_pleiepenger_sykt_barn_skal_ikke_tas_med() {
		// Arrange
        var startForeldrepermisjon = LocalDate.of(2023, 1, 1);
        var sluttForeldrepermisjon = LocalDate.of(2023, 1, 20);
		var foreldrepermisjon = lagForeldrepermisjon(startForeldrepermisjon, sluttForeldrepermisjon);
		var yrkesaktivitet = lagYrkesaktivitet(foreldrepermisjon);
		final var pleiepengerSyktBarn = new LocalDateTimeline<Boolean>(startForeldrepermisjon, sluttForeldrepermisjon, true);
		var skjæringstidspunkt = LocalDate.of(2023, 1, 21);

		// Act
        var permisjonTidslinje = PermisjonPerYrkesaktivitet.utledPermisjonPerYrkesaktivitet(yrkesaktivitet, Map.of(YtelseType.PLEIEPENGER_SYKT_BARN, pleiepengerSyktBarn), skjæringstidspunkt);

		// Assert
		assertTrue(permisjonTidslinje.isEmpty());
	}

	@Test
	void foreldrepermisjon_som_overlapper_pleiepenger_livets_sluttface_skal_ikke_tas_med() {
		// Arrange
        var startForeldrepermisjon = LocalDate.of(2023, 1, 1);
        var sluttForeldrepermisjon = LocalDate.of(2023, 1, 20);
		var foreldrepermisjon = lagForeldrepermisjon(startForeldrepermisjon, sluttForeldrepermisjon);
		var yrkesaktivitet = lagYrkesaktivitet(foreldrepermisjon);
		final var pleiepengerLivetsSluttfase = new LocalDateTimeline<Boolean>(startForeldrepermisjon, sluttForeldrepermisjon, true);
		var skjæringstidspunkt = LocalDate.of(2023, 1, 21);

		// Act
        var permisjonTidslinje = PermisjonPerYrkesaktivitet.utledPermisjonPerYrkesaktivitet(yrkesaktivitet, Map.of(YtelseType.PLEIEPENGER_NÆRSTÅENDE, pleiepengerLivetsSluttfase), skjæringstidspunkt);

		// Assert
		assertTrue(permisjonTidslinje.isEmpty());
	}




	private static YrkesaktivitetDto lagYrkesaktivitet(PermisjonDtoBuilder foreldrepermisjon) {
		return YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
				.leggTilPermisjon(foreldrepermisjon)
				.build();
	}

	private static PermisjonDtoBuilder lagForeldrepermisjon(LocalDate startForeldrepermisjon, LocalDate sluttForeldrepermisjon) {
		return PermisjonDtoBuilder.ny()
				.medPeriode(Intervall.fraOgMedTilOgMed(startForeldrepermisjon, sluttForeldrepermisjon))
				.medProsentsats(Stillingsprosent.HUNDRED)
				.medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.PERMISJON_MED_FORELDREPENGER);
	}


}
