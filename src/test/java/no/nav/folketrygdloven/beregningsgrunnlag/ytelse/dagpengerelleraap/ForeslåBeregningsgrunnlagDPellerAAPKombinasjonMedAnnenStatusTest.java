package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.dagpengerelleraap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusMedHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;

class ForeslåBeregningsgrunnlagDPellerAAPKombinasjonMedAnnenStatusTest {


	public static final LocalDate STP = LocalDate.of(2020, 4, 1);


	@Test
	void skal_beregne_dagpenger_fra_meldekort() {
		// Arrange
		var dpStatus = lagStatus(AktivitetStatus.DP);
		var arbeid = lagArbeid();
		var p = Periode.of(STP, STP.plusMonths(1));
		var periode = lagPeriodeMedStatus(List.of(dpStatus, arbeid), p);
		var dagsats = BigDecimal.valueOf(1000);
		var utbetalingsgrad = BigDecimal.valueOf(1);
		var periodeinntektDagpenger = lagPeriodeInntektFraMeldekort(p, dagsats, utbetalingsgrad);
		var inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(periodeinntektDagpenger);
		byggBG(periode, inntektsgrunnlag);

		// Act
		kjørRegel(periode);

		// Assert
		assertThat(dpStatus.getBeregnetPrÅr()).isCloseTo(BigDecimal.valueOf(260_000), Percentage.withPercentage(0.00001));
	}


	@Test
	void skal_beregne_dagpenger_fra_meldekort_med_gradert_utbetaling() {
		// Arrange
		var dpStatus = lagStatus(AktivitetStatus.DP);
		var arbeid = lagArbeid();
		var p = Periode.of(STP, STP.plusMonths(1));
		var periode = lagPeriodeMedStatus(List.of(dpStatus, arbeid), p);
		var dagsats = BigDecimal.valueOf(1000);
		var utbetalingsgrad = BigDecimal.valueOf(0.5);
		var periodeinntektDagpenger = lagPeriodeInntektFraMeldekort(p, dagsats, utbetalingsgrad);
		var inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(periodeinntektDagpenger);
		byggBG(periode, inntektsgrunnlag);

		// Act
		kjørRegel(periode);

		// Assert
		assertThat(dpStatus.getBeregnetPrÅr()).isCloseTo(BigDecimal.valueOf(130_000), Percentage.withPercentage(0.00001));
	}


	@Test
	void skal_beregne_fra_ytelse_vedtak_ved_direkte_overgang() {
		// Arrange
		var dpStatus = lagStatus(AktivitetStatus.PSB_AV_DP);
		var arbeid = lagArbeid();
		var p = Periode.of(STP, STP.plusMonths(1));
		var periode = lagPeriodeMedStatus(List.of(dpStatus, arbeid), p);
		var dagsats = BigDecimal.valueOf(1000);
		var utbetalingsgrad = BigDecimal.valueOf(1);
		var periodeinntekt = lagPeriodeinntektFraYtelse(p, dagsats, utbetalingsgrad, Inntektskategori.DAGPENGER);
		var inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(periodeinntekt);
		byggBG(periode, inntektsgrunnlag);

		// Act
		kjørRegel(periode);

		// Assert
		assertThat(dpStatus.getBeregnetPrÅr()).isCloseTo(BigDecimal.valueOf(260_000), Percentage.withPercentage(0.00001));
	}

	@Test
	void skal_beregne_fra_ytelse_vedtak_ved_direkte_overgang_i_kombinasjon_med_arbeid() {
		// Arrange
		var dpStatus = lagStatus(AktivitetStatus.PSB_AV_DP);
		var arbeid = lagArbeid();
		var p = Periode.of(STP, STP.plusMonths(1));
		var periode = lagPeriodeMedStatus(List.of(dpStatus, arbeid), p);
		var dagsats = BigDecimal.valueOf(1000);
		var utbetalingsgrad = BigDecimal.valueOf(1);
		var periodeinntektDagpenger = lagPeriodeinntektFraYtelse(p, dagsats, utbetalingsgrad, Inntektskategori.DAGPENGER);
		var periodeinntektArbeid = lagPeriodeinntektFraYtelse(p, BigDecimal.TEN, utbetalingsgrad, Inntektskategori.ARBEIDSTAKER);
		var inntektsgrunnlag = new Inntektsgrunnlag();
		inntektsgrunnlag.leggTilPeriodeinntekt(periodeinntektDagpenger);
		inntektsgrunnlag.leggTilPeriodeinntekt(periodeinntektArbeid);
		byggBG(periode, inntektsgrunnlag);

		// Act
		kjørRegel(periode);

		// Assert
		assertThat(dpStatus.getBeregnetPrÅr()).isCloseTo(BigDecimal.valueOf(260_000), Percentage.withPercentage(0.00001));
	}

	private Periodeinntekt lagPeriodeinntektFraYtelse(Periode p, BigDecimal dagsats, BigDecimal utbetalingsgrad, Inntektskategori dagpenger) {
		return Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.YTELSE_VEDTAK)
				.medPeriode(p)
				.medInntekt(dagsats)
				.medUtbetalingsfaktor(utbetalingsgrad)
				.medInntektskategori(dagpenger)
				.build();
	}


	private BeregningsgrunnlagPeriode lagPeriodeMedStatus(List<BeregningsgrunnlagPrStatus> statuser, Periode p) {
		var periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(p);
		statuser.forEach(periode::medBeregningsgrunnlagPrStatus);
		return periode.build();
	}

	private Beregningsgrunnlag byggBG(BeregningsgrunnlagPeriode periode, Inntektsgrunnlag inntektsgrunnlag) {
		return Beregningsgrunnlag.builder()
				.medAktivitetStatuser(periode.getBeregningsgrunnlagPrStatus().stream().map(s -> new AktivitetStatusMedHjemmel(s.getAktivitetStatus(), null)).toList())
				.medInntektsgrunnlag(inntektsgrunnlag)
				.medBeregningsgrunnlagPeriode(periode)
				.medSkjæringstidspunkt(STP)
				.medGrunnbeløp(BigDecimal.TEN)
				.medGrunnbeløpSatser(List.of(new Grunnbeløp(STP.minusMonths(12), STP, 10L, 10L)))
				.build();
	}

	private Evaluation kjørRegel(BeregningsgrunnlagPeriode periode) {
		return new ForeslåBeregningsgrunnlagDPellerAAPKombinasjonMedAnnenStatus().evaluate(periode);
	}

	private BeregningsgrunnlagPrStatus lagArbeid() {
		return BeregningsgrunnlagPrStatus.builder()
				.medAktivitetStatus(AktivitetStatus.ATFL)
				.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
						.medAndelNr(2L)
						.medArbeidsforhold(Arbeidsforhold.builder()
								.medOrgnr("123456789")
								.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT).build()).build())
				.build();
	}

	private BeregningsgrunnlagPrStatus lagStatus(AktivitetStatus dp) {
		return BeregningsgrunnlagPrStatus.builder()
				.medAndelNr(1L)
				.medAktivitetStatus(dp)
				.build();
	}

	private Periodeinntekt lagPeriodeInntektFraMeldekort(Periode p, BigDecimal dagsats, BigDecimal utbetalingsgrad) {
		return Periodeinntekt.builder()
				.medInntektskildeOgPeriodeType(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP)
				.medPeriode(p)
				.medInntekt(dagsats)
				.medUtbetalingsfaktor(utbetalingsgrad)
				.build();
	}

}