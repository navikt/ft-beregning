package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.adapter.ErSøktYtelseFor;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

class ErSøktYtelseForTest {

	@Test
	void skal_gi_søkt_ytelse_for_lik_false_når_utbetalingsgrad_er_lik_null_og_aktivitetsgrad_ikke_er_satt() {
		var intervall = Intervall.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now());
		var erSøktYtelseFor = ErSøktYtelseFor.erSøktYtelseFor(new PeriodeMedUtbetalingsgradDto(intervall, Utbetalingsgrad.ZERO, null));
		assertThat(erSøktYtelseFor).isFalse();
	}

	@Test
	void skal_gi_søkt_ytelse_for_lik_false_når_utbetalingsgrad_er_lik_null_og_aktivitetsgrad_lik_hundre() {
		var intervall = Intervall.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now());
		var erSøktYtelseFor = ErSøktYtelseFor.erSøktYtelseFor(new PeriodeMedUtbetalingsgradDto(intervall, Utbetalingsgrad.ZERO, Aktivitetsgrad.HUNDRE));
		assertThat(erSøktYtelseFor).isFalse();
	}

	@Test
	void skal_gi_søkt_ytelse_for_lik_true_når_utbetalingsgrad_er_over_null_og_aktivitetsgrad_ikke_satt() {
		var intervall = Intervall.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now());
		var erSøktYtelseFor = ErSøktYtelseFor.erSøktYtelseFor(new PeriodeMedUtbetalingsgradDto(intervall, Utbetalingsgrad.valueOf(1), null));
		assertThat(erSøktYtelseFor).isTrue();
	}

	@Test
	void skal_gi_søkt_ytelse_for_lik_true_når_utbetalingsgrad_er_over_null_og_aktivitetsgrad_lik_hundre() {
		// Feilsituasjon, f.eks avrundingsfeil i aktivitetsgrad eller utbetalingsgrad. prio til utbetalingsgrad
		var intervall = Intervall.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now());
		var erSøktYtelseFor = ErSøktYtelseFor.erSøktYtelseFor(new PeriodeMedUtbetalingsgradDto(intervall, Utbetalingsgrad.valueOf(1), Aktivitetsgrad.HUNDRE));
		assertThat(erSøktYtelseFor).isTrue();
	}

	@Test
	void skal_gi_søkt_ytelse_for_lik_true_når_utbetalingsgrad_er_over_null_og_aktivitetsgrad_er_under_hundre() {
		var intervall = Intervall.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now());
		var erSøktYtelseFor = ErSøktYtelseFor.erSøktYtelseFor(new PeriodeMedUtbetalingsgradDto(intervall, Utbetalingsgrad.valueOf(1), Aktivitetsgrad.fra(99)));
		assertThat(erSøktYtelseFor).isTrue();
	}

	@Test
	void skal_gi_søkt_ytelse_for_lik_true_når_utbetalingsgrad_er_null_og_aktivitetsgrad_er_under_hundre() {
		var intervall = Intervall.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now());
		var erSøktYtelseFor = ErSøktYtelseFor.erSøktYtelseFor(new PeriodeMedUtbetalingsgradDto(intervall, Utbetalingsgrad.ZERO, Aktivitetsgrad.fra(99)));
		assertThat(erSøktYtelseFor).isTrue();
	}


	@Test
	void skal_gi_søkt_ytelse_for_lik_false_når_utbetalingsgrad_er_null_og_aktivitetsgrad_er_hundre() {
		var intervall = Intervall.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now());
		var erSøktYtelseFor = ErSøktYtelseFor.erSøktYtelseFor(new PeriodeMedUtbetalingsgradDto(intervall, Utbetalingsgrad.ZERO, Aktivitetsgrad.HUNDRE));
		assertThat(erSøktYtelseFor).isFalse();
	}


}