package no.nav.folketrygdloven.beregningsgrunnlag.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class VirkedagerTest {

	@Test
	void skal_beregne_virkedager_fom_lik_tom() {
		var dager = Virkedager.beregnAntallVirkedager(LocalDate.of(2023, 3, 22), LocalDate.of(2023, 3, 22));

		assertThat(dager).isEqualTo(1);
	}

	@Test
	void skal_beregne_virkedager_fom_ulik_tom_over_helg() {
		var dager = Virkedager.beregnAntallVirkedager(LocalDate.of(2023, 3, 22), LocalDate.of(2023, 3, 31));

		assertThat(dager).isEqualTo(8);
	}

	@Test
	void kaster_feil_ved_tom_før_fom() {
		var fom = LocalDate.of(2023, 3, 22);
		var tom = LocalDate.of(2023, 3, 21);
		Assertions.assertThrows(IllegalArgumentException.class, () -> Virkedager.beregnAntallVirkedager(fom, tom));
	}
}