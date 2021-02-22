package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

class SkalOmfordeleFraBrukersAndelTilFLEllerSNTest {

	@Test
	void skal_ikke_omfordele_fra_brukers_andel_uten_brukers_andel() {
		// Arrange
		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.SN)
						.medInntektskategori(Inntektskategori.UDEFINERT)
						.medAndelNr(1L)
						.build())
				.build();

		// Act
		Evaluation evaluation = new SkalOmfordeleFraBrukersAndelTilFLEllerSN().evaluate(periode);

		// Assert
		assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
	}

	@Test
	void skal_ikke_omfordele_fra_brukers_andel_uten_fl_eller_sn() {
		// Arrange
		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.BA)
						.medBeregnetPrÅr(BigDecimal.valueOf(200_000))
						.medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
						.medAndelNr(1L)
						.build())
				.build();

		// Act
		Evaluation evaluation = new SkalOmfordeleFraBrukersAndelTilFLEllerSN().evaluate(periode);

		// Assert
		assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
	}

	@Test
	void skal_omfordele_fra_brukers_andel_med_fl() {
		// Arrange
		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.BA)
						.medBeregnetPrÅr(BigDecimal.valueOf(200_000))
						.medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
						.medAndelNr(1L)
						.build())
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.ATFL)
						.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
								.medInntektskategori(Inntektskategori.UDEFINERT)
								.medArbeidsforhold(Arbeidsforhold.builder()
										.medAktivitet(Aktivitet.FRILANSINNTEKT)
										.build())
								.medAndelNr(2L)
								.build())
						.build())
				.build();

		// Act
		Evaluation evaluation = new SkalOmfordeleFraBrukersAndelTilFLEllerSN().evaluate(periode);

		// Assert
		assertThat(evaluation.result()).isEqualTo(Resultat.JA);
	}

	@Test
	void skal_omfordele_fra_brukers_andel_med_sn() {
		// Arrange
		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.BA)
						.medBeregnetPrÅr(BigDecimal.valueOf(200_000))
						.medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
						.medAndelNr(1L)
						.build())
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.SN)
						.medInntektskategori(Inntektskategori.UDEFINERT)
						.medAndelNr(2L)
						.build())
				.build();

		// Act
		Evaluation evaluation = new SkalOmfordeleFraBrukersAndelTilFLEllerSN().evaluate(periode);

		// Assert
		assertThat(evaluation.result()).isEqualTo(Resultat.JA);
	}

	@Test
	void skal_omfordele_fra_brukers_andel_med_fl_og_sn() {
		// Arrange
		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.BA)
						.medBeregnetPrÅr(BigDecimal.valueOf(200_000))
						.medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
						.medAndelNr(1L)
						.build())
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.ATFL)
						.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
								.medInntektskategori(Inntektskategori.UDEFINERT)
								.medArbeidsforhold(Arbeidsforhold.builder()
										.medAktivitet(Aktivitet.FRILANSINNTEKT)
										.build())
								.medAndelNr(2L)
								.build())
						.build())
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.SN)
						.medInntektskategori(Inntektskategori.UDEFINERT)
						.medAndelNr(3L)
						.build())
				.build();

		// Act
		Evaluation evaluation = new SkalOmfordeleFraBrukersAndelTilFLEllerSN().evaluate(periode);

		// Assert
		assertThat(evaluation.result()).isEqualTo(Resultat.JA);
	}


	@Test
	void skal_ikkje_omfordele_fra_brukers_andel_med_kun_arbeidstaker() {
		// Arrange
		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.BA)
						.medBeregnetPrÅr(BigDecimal.valueOf(200_000))
						.medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
						.medAndelNr(1L)
						.build())
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.ATFL)
						.medArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold.builder()
								.medInntektskategori(Inntektskategori.UDEFINERT)
								.medArbeidsforhold(Arbeidsforhold.builder()
										.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
										.medOrgnr("123423874")
										.build())
								.medAndelNr(2L)
								.build())
						.build())
				.build();

		// Act
		Evaluation evaluation = new SkalOmfordeleFraBrukersAndelTilFLEllerSN().evaluate(periode);

		// Assert
		assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
	}


}