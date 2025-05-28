package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Dekningsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.FrisinnGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.FrisinnPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;

class FinnGrenseverdiForTotalOver6GTest {

	public static final String ORGNR = "999999999";
	public static final int GRUNNBELØP = 100_000;
	public static final BigDecimal SEKS_G = BigDecimal.valueOf(GRUNNBELØP * 6);

	private static final LocalDate skjæringstidspunkt = LocalDate.of(2020, Month.MARCH, 15);

	@Test
	void kun_frilans_uten_rest_fra_avkorting_fører_til_ingen_endring_i_grenseverdi_for_neste_periode() {
		// Arrange
		var p1 = lagBeregningsgrunnlagPeriode(
				0d,
				200_000d,
				500_000d,
				0d,
				0d,
				new Periode(skjæringstidspunkt, skjæringstidspunkt.plusMonths(1)));
		var p2 = lagBeregningsgrunnlagPeriode(
				0d,
				200_000d,
				500_000d,
				0d,
				50d,
				new Periode(skjæringstidspunkt.plusMonths(1).plusDays(1), null));
		lagBeregningsgrunnlag(List.of(p1, p2), true, true);

		// Act
		kjørRegel(p1);

		// Assert
		assertThat(p2.getGrenseverdi()).isEqualByComparingTo(SEKS_G);
	}

	@Test
	void kun_frilans_med_rest_fra_avkorting_for_nest_siste_periode_fører_til_ingen_endring_i_grenseverdi_for_neste_periode() {
		// Arrange
		var p1 = lagBeregningsgrunnlagPeriode(
				0d,
				200_000d,
				500_000d,
				0d,
				75d,
				new Periode(skjæringstidspunkt, skjæringstidspunkt.plusMonths(1)));
		var p2 = lagBeregningsgrunnlagPeriode(
				0d,
				200_000d,
				500_000d,
				0d,
				0d,
				new Periode(skjæringstidspunkt.plusMonths(1).plusDays(1), null));
		lagBeregningsgrunnlag(List.of(p1, p2), true, true);

		// Act
		kjørRegel(p1);

		// Assert
		assertThat(p2.getGrenseverdi()).isEqualByComparingTo(SEKS_G);
	}

	@Test
	void frilans_med_rest_fra_avkorting_endring_i_grenseverdi_for_neste_periode() {
		// Arrange
        var virkedagerFørstePeriode = 19;
		var p1 = lagBeregningsgrunnlagPeriode(
				0d,
				260_000d,
				SEKS_G.doubleValue(),
				0d,
				50d,
				new Periode(skjæringstidspunkt, Virkedager.plusVirkedager(skjæringstidspunkt, virkedagerFørstePeriode)));
        var antallVirkerdagerTestPeriode = 9;
		var test_periode = lagTestPeriode(p1, antallVirkerdagerTestPeriode, AktivitetStatus.ATFL);
		lagBeregningsgrunnlag(List.of(p1, test_periode), true, true);

		// Act
		kjørRegel(p1);

		// Assert
		assertThat(test_periode.getGrenseverdi()).isEqualByComparingTo(SEKS_G.subtract(BigDecimal.valueOf(260_000)));
	}

	@Test
	void frilans_med_rest_fra_avkorting_ikke_søkt_fl_ingen_endring_i_grenseverdi_for_neste_periode() {
		// Arrange
        var virkedagerFørstePeriode = 19;
		var p1 = lagBeregningsgrunnlagPeriode(
				0d,
				260_000d,
				SEKS_G.doubleValue(),
				0d,
				50d,
				new Periode(skjæringstidspunkt, Virkedager.plusVirkedager(skjæringstidspunkt, virkedagerFørstePeriode)));
        var antallVirkerdagerTestPeriode = 9;
		var test_periode = lagTestPeriode(p1, antallVirkerdagerTestPeriode, AktivitetStatus.ATFL);
		lagBeregningsgrunnlag(List.of(p1, test_periode), false, true);

		// Act
		kjørRegel(p1);

		// Assert
		assertThat(test_periode.getGrenseverdi()).isEqualByComparingTo(SEKS_G);
	}

	@Test
	void næring_med_rest_fra_avkorting_endring_i_grenseverdi_for_neste_periode() {
		// Arrange
        var virkedagerFørstePeriode = 19;
		var p1 = lagBeregningsgrunnlagPeriode(
				260_000d,
				0d,
				SEKS_G.doubleValue(),
				50d,
				0d,
				new Periode(skjæringstidspunkt, Virkedager.plusVirkedager(skjæringstidspunkt, virkedagerFørstePeriode)));
        var antallVirkerdagerTestPeriode = 9;
		var test_periode = lagTestPeriode(p1, antallVirkerdagerTestPeriode, AktivitetStatus.SN);
		lagBeregningsgrunnlag(List.of(p1, test_periode), true, true);

		// Act
		kjørRegel(p1);

		// Assert
		assertThat(test_periode.getGrenseverdi()).isEqualByComparingTo(SEKS_G.subtract(BigDecimal.valueOf(260_000)));
	}

	@Test
	void næring_med_rest_fra_avkorting_ikke_søkt_næring_ingen_endring_i_grenseverdi_for_neste_periode() {
		// Arrange
        var virkedagerFørstePeriode = 19;
		var p1 = lagBeregningsgrunnlagPeriode(
				260_000d,
				0d,
				SEKS_G.doubleValue(),
				50d,
				0d,
				new Periode(skjæringstidspunkt, Virkedager.plusVirkedager(skjæringstidspunkt, virkedagerFørstePeriode)));
        var antallVirkerdagerTestPeriode = 9;
		var test_periode = lagTestPeriode(p1, antallVirkerdagerTestPeriode, AktivitetStatus.SN);
		lagBeregningsgrunnlag(List.of(p1, test_periode), true, false);

		// Act
		kjørRegel(p1);

		// Assert
		assertThat(test_periode.getGrenseverdi()).isEqualByComparingTo(SEKS_G);
	}

	@Test
	void tester_at_fl_sn_uttak_på_sn_rest_overføres_til_neste_periode() {
		// Arrange
        var virkedagerFørstePeriode = 12;
		var p1 = lagBeregningsgrunnlagPeriode(
				200_000d,
				300_000d,
				200_000d,
				40.9d,
				0d,
				new Periode(skjæringstidspunkt, Virkedager.plusVirkedager(skjæringstidspunkt, virkedagerFørstePeriode)));
        var antallVirkerdagerTestPeriode = 10;
		var test_periode = lagTestPeriode(p1, antallVirkerdagerTestPeriode, AktivitetStatus.SN);
		lagBeregningsgrunnlag(List.of(p1, test_periode), true, true);

		// Act
		kjørRegel(p1);

		// Assert
		assertThat(test_periode.getGrenseverdi()).isEqualByComparingTo(SEKS_G.subtract(BigDecimal.valueOf(21509.0909090980)));
	}

	@Test
	void tester_at_fl_sn_uttak_på_sn_rest_ikke_overføres_til_neste_periode_ved_månedslutt() {
		// Arrange
		var p1 = lagBeregningsgrunnlagPeriode(
				200_000d,
				300_000d,
				200_000d,
				40.9d,
				0d,
				new Periode(skjæringstidspunkt, LocalDate.of(2020, 4, 30)));
        var antallVirkerdagerTestPeriode = 10;
		var test_periode = lagTestPeriode(p1, antallVirkerdagerTestPeriode, AktivitetStatus.SN);
		lagBeregningsgrunnlag(List.of(p1, test_periode), true, true);

		// Act
		kjørRegel(p1);

		// Assert
		assertThat(test_periode.getGrenseverdi()).isEqualByComparingTo(SEKS_G);
	}


	private void kjørRegel(BeregningsgrunnlagPeriode grunnlag) {
        var grenseregel = new FinnGrenseverdiForTotalOver6G();
		grenseregel.evaluate(grunnlag);
	}

	private BeregningsgrunnlagPeriode lagBeregningsgrunnlagPeriode(Double snInntektPrÅr,
	                                                               Double frilansInntektPrÅr,
	                                                               Double arbeidsinntektPrÅr,
	                                                               Double snUtbetalingsgrad,
	                                                               Double flUtbetalingsgrad,
	                                                               Periode periode) {
        var periodeBuilder = BeregningsgrunnlagPeriode.builder()
				.medPeriode(periode);
		byggSN(snInntektPrÅr, periodeBuilder, snUtbetalingsgrad);
		byggATFL(frilansInntektPrÅr, arbeidsinntektPrÅr, periodeBuilder, flUtbetalingsgrad);
		return periodeBuilder.build();
	}

	private BeregningsgrunnlagPeriode lagTestPeriode(BeregningsgrunnlagPeriode periode, int virkedager, AktivitetStatus statusSøktFor) {
        var tom = periode.getBeregningsgrunnlagPeriode().getTom();
        var p = new Periode(tom.plusDays(1), Virkedager.plusVirkedager(tom.plusDays(1), virkedager));
        var periodeBuilder = BeregningsgrunnlagPeriode.builder().medPeriode(p);
		if (AktivitetStatus.ATFL.equals(statusSøktFor)) {
			byggATFL(0d, 0d, periodeBuilder, 50d);
		} else if (AktivitetStatus.SN.equals(statusSøktFor)) {
			byggSN(0d, periodeBuilder, 50d);
		} else {
			byggATFL(0d, 0d, periodeBuilder, 0d);
		}
		return periodeBuilder.build();
	}


	private Beregningsgrunnlag lagBeregningsgrunnlag(List<BeregningsgrunnlagPeriode> perioder, boolean søkerYtelseFrilans, boolean søkerYtelseNæring) {
		perioder.forEach(periode -> BeregningsgrunnlagPeriode.oppdater(periode).medDekningsgrad(Dekningsgrad.DEKNINGSGRAD_80));
		return Beregningsgrunnlag.builder()
				.medGrunnbeløp(BigDecimal.valueOf(GRUNNBELØP))
				.medAktivitetStatuser(List.of(new AktivitetStatusMedHjemmel(AktivitetStatus.ATFL_SN, null)))
				.medBeregningsgrunnlagPerioder(perioder)
				.medYtelsesSpesifiktGrunnlag(new FrisinnGrunnlag(List.of(new FrisinnPeriode(perioder.get(0).getBeregningsgrunnlagPeriode(), søkerYtelseFrilans, søkerYtelseNæring))))
				.build();
	}

	private void byggSN(Double snInntektPrÅr, BeregningsgrunnlagPeriode.Builder periodeBuilder, Double utbetalingsgrad) {
		if (snInntektPrÅr != null) {
			periodeBuilder.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
					.medAktivitetStatus(AktivitetStatus.SN)
					.medAndelNr(1L)
					.medUtbetalingsprosent(BigDecimal.valueOf(utbetalingsgrad))
					.medBruttoPrÅr(BigDecimal.valueOf(snInntektPrÅr))
					.build());
		}
	}

	private void byggATFL(Double frilansInntektPrÅr, Double arbeidsinntektPrÅr, BeregningsgrunnlagPeriode.Builder periodeBuilder, Double flUtbetalingsgrad) {
		if (frilansInntektPrÅr != null || arbeidsinntektPrÅr != null) {
            var atflStatusBuilder = BeregningsgrunnlagPrStatus.builder()
					.medAktivitetStatus(AktivitetStatus.ATFL);
			if (frilansInntektPrÅr != null) {
                var flAndel = BeregningsgrunnlagPrArbeidsforhold.builder()
						.medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
						.medBruttoPrÅr(BigDecimal.valueOf(frilansInntektPrÅr))
						.medAndelNr(2L)
						.medUtbetalingsprosent(BigDecimal.valueOf(flUtbetalingsgrad))
						.build();
				flAndel.setErSøktYtelseFor(true);
				atflStatusBuilder.medArbeidsforhold(flAndel);
			}
			if (arbeidsinntektPrÅr != null) {
                var arbfor = BeregningsgrunnlagPrArbeidsforhold.builder()
						.medArbeidsforhold(Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(ORGNR))
						.medBruttoPrÅr(BigDecimal.valueOf(arbeidsinntektPrÅr))
						.medAndelNr(3L)
						.build();
				arbfor.setErSøktYtelseFor(false);
				atflStatusBuilder.medArbeidsforhold(arbfor);
			}
			periodeBuilder.medBeregningsgrunnlagPrStatus(atflStatusBuilder.build());
		}
	}

}
