package no.nav.folketrygdloven.skjæringstidspunkt.status;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFP;

import no.nav.fpsak.nare.evaluation.Evaluation;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

class FastsettStatusOgAndelPrPeriodePSBTest {
	private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
	private static final String ARBEIDSGIVER_ORGNR_1 = "123456789";
	private static final String ARBEIDSGIVER_ORGNR_2 = "987654321";

	@Test
	public void ta_med_aktivitet_som_starter_på_skjæringstidspunkt_for_beregning_med_et_arbeidsforhold() {
		// Arrange
		var aktivPeriode = lagAktivPeriode(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE, ARBEIDSGIVER_ORGNR_1);
		var regelmodell = lagAktivitetStatusModellForEttArbeidsforhold(aktivPeriode);

		// Act
		Evaluation evaluation = new FastsettStatusOgAndelPrPeriode().evaluate(regelmodell);

		// Assert
		assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe().size()).isEqualTo(1);
	}

	@Test
	public void ta_med_aktiviter_som_starter_før_skjæringstidspunkt_og_fortsetter_etter_for_beregning_med_et_arbeidsforhold() {
		// Arrange
		var aktivPeriode = lagAktivPeriode(SKJÆRINGSTIDSPUNKT.minusMonths(1), TIDENES_ENDE, ARBEIDSGIVER_ORGNR_1);
		AktivitetStatusModell regelmodell = lagAktivitetStatusModellForEttArbeidsforhold(aktivPeriode);

		// Act
		Evaluation evaluation = new FastsettStatusOgAndelPrPeriode().evaluate(regelmodell);

		// Assert
		assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe().size()).isEqualTo(1);
	}

	@Test
	public void ta_med_aktiviter_som_starter_før_og_på_skjæringstidspunkt_og_fortsetter_etter_for_beregning_med_to_arbeidsforhold() {
		// Arrange
		var aktivPeriode1 = lagAktivPeriode(SKJÆRINGSTIDSPUNKT, TIDENES_ENDE, ARBEIDSGIVER_ORGNR_1);
		var aktivPeriode2 = lagAktivPeriode(SKJÆRINGSTIDSPUNKT.minusMonths(1), TIDENES_ENDE, ARBEIDSGIVER_ORGNR_2);
		var regelmodell = lagAktivitetStatusModellForToArbeidsforhold(aktivPeriode1, aktivPeriode2);

		// Act
		Evaluation evaluation = new FastsettStatusOgAndelPrPeriode().evaluate(regelmodell);

		// Assert
		assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe().size()).isEqualTo(2);
	}

	@Test
	public void ikke_ta_med_aktiviter_som_slutter_en_dag_før_skjæringstidspunkt_for_beregning_med_et_arbeidsforhold() {
		// Arrange
		var aktivPeriode = lagAktivPeriode(SKJÆRINGSTIDSPUNKT.minusMonths(10), SKJÆRINGSTIDSPUNKT.minusDays(1), ARBEIDSGIVER_ORGNR_1);
		var regelmodell = lagAktivitetStatusModellForEttArbeidsforhold(aktivPeriode);

		// Act
		Evaluation evaluation = new FastsettStatusOgAndelPrPeriode().evaluate(regelmodell);

		// Assert
		assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe().size()).isEqualTo(0);
	}

	@Test
	public void ikke_ta_med_aktiviter_som_slutter_en_eller_flere_dager_før_skjæringstidspunkt_for_beregning_med_to_arbeidsforhold_() {
		// Arrange
		AktivPeriode aktivPeriode1 = lagAktivPeriode(SKJÆRINGSTIDSPUNKT.minusMonths(10), SKJÆRINGSTIDSPUNKT.minusDays(1), ARBEIDSGIVER_ORGNR_1);
		AktivPeriode aktivPeriode2 = lagAktivPeriode(SKJÆRINGSTIDSPUNKT.minusMonths(9), SKJÆRINGSTIDSPUNKT.minusMonths(2), ARBEIDSGIVER_ORGNR_2);
		AktivitetStatusModell regelmodell = lagAktivitetStatusModellForToArbeidsforhold(aktivPeriode1, aktivPeriode2);

		// Act
		Evaluation evaluation = new FastsettStatusOgAndelPrPeriode().evaluate(regelmodell);

		// Assert
		assertThat(regelmodell.getBeregningsgrunnlagPrStatusListe().size()).isEqualTo(0);
	}

	private AktivPeriode lagAktivPeriode(LocalDate fom, LocalDate tom, String arbeidsgiverOrgnr) {
		return AktivPeriode.forArbeidstakerHosVirksomhet(Periode.of(fom, tom), arbeidsgiverOrgnr, null);
	}

	private AktivitetStatusModell lagAktivitetStatusModell() {
		var regelmodell = new AktivitetStatusModell();
		regelmodell = new AktivitetStatusModellFP();
		regelmodell.setFinnBeregningstidspunkt((stp) -> stp);
		regelmodell.setSkjæringstidspunktForBeregning(SKJÆRINGSTIDSPUNKT);

		return regelmodell;
	}

	private AktivitetStatusModell lagAktivitetStatusModellForEttArbeidsforhold(AktivPeriode aktivPeriode) {
		var regelmodell = lagAktivitetStatusModell();
		regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode);

		return regelmodell;
	}

	private AktivitetStatusModell lagAktivitetStatusModellForToArbeidsforhold(AktivPeriode aktivPeriode1, AktivPeriode aktivPeriode2) {
		var regelmodell = lagAktivitetStatusModell();
		regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode1);
		regelmodell.leggTilEllerOppdaterAktivPeriode(aktivPeriode2);

		return regelmodell;
	}

}