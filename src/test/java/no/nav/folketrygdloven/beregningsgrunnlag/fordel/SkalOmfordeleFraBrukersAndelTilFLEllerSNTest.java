package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.SkalOmfordeleFraBrukersAndelTilFLEllerSN;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;

class SkalOmfordeleFraBrukersAndelTilFLEllerSNTest {

	@Test
	void skal_ikke_omfordele_fra_brukers_andel_uten_brukers_andel() {
		// Arrange
		var andel = FordelAndelModell.builder()
				.medAktivitetStatus(AktivitetStatus.SN)
				.medInntektskategori(Inntektskategori.UDEFINERT)
				.medAndelNr(1L)
				.build();
		var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), TIDENES_ENDE), Collections.singletonList(andel));

		// Act
		Evaluation evaluation = new SkalOmfordeleFraBrukersAndelTilFLEllerSN().evaluate(periode);

		// Assert
		assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
	}

	@Test
	void skal_ikke_omfordele_fra_brukers_andel_uten_fl_eller_sn() {
		// Arrange
		var andel = FordelAndelModell.builder()
				.medAktivitetStatus(AktivitetStatus.BA)
				.medForeslåttPrÅr(BigDecimal.valueOf(200_000))
				.medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
				.medAndelNr(1L)
				.build();
		var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), TIDENES_ENDE), Collections.singletonList(andel));

		// Act
		Evaluation evaluation = new SkalOmfordeleFraBrukersAndelTilFLEllerSN().evaluate(periode);

		// Assert
		assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
	}

	@Test
	void skal_omfordele_fra_brukers_andel_med_fl() {
		// Arrange
		var andel1 = FordelAndelModell.builder()
				.medAktivitetStatus(AktivitetStatus.BA)
				.medForeslåttPrÅr(BigDecimal.valueOf(200_000))
				.medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
				.medAndelNr(1L)
				.build();
		var andel2 = FordelAndelModell.builder()
				.medAktivitetStatus(AktivitetStatus.FL)
				.medInntektskategori(Inntektskategori.UDEFINERT)
				.medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
				.medAndelNr(2L)
				.build();
		var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), TIDENES_ENDE), Arrays.asList(andel1, andel2));

		// Act
		Evaluation evaluation = new SkalOmfordeleFraBrukersAndelTilFLEllerSN().evaluate(periode);

		// Assert
		assertThat(evaluation.result()).isEqualTo(Resultat.JA);
	}

	@Test
	void skal_omfordele_fra_brukers_andel_med_sn() {
		// Arrange
		var andelBA = FordelAndelModell.builder()
				.medAktivitetStatus(AktivitetStatus.BA)
				.medForeslåttPrÅr(BigDecimal.valueOf(200_000))
				.medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
				.medAndelNr(1L)
				.build();
		var andelSN = FordelAndelModell.builder()
				.medAktivitetStatus(AktivitetStatus.SN)
				.medInntektskategori(Inntektskategori.UDEFINERT)
				.medAndelNr(2L)
				.build();
		var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), TIDENES_ENDE), Arrays.asList(andelBA, andelSN));

		// Act
		Evaluation evaluation = new SkalOmfordeleFraBrukersAndelTilFLEllerSN().evaluate(periode);

		// Assert
		assertThat(evaluation.result()).isEqualTo(Resultat.JA);
	}

	@Test
	void skal_omfordele_fra_brukers_andel_med_fl_og_sn() {
		// Arrange
		var andelBA = FordelAndelModell.builder()
				.medAktivitetStatus(AktivitetStatus.BA)
				.medForeslåttPrÅr(BigDecimal.valueOf(200_000))
				.medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
				.medAndelNr(1L)
				.build();
		var andelFL = FordelAndelModell.builder()
				.medAktivitetStatus(AktivitetStatus.FL)
				.medInntektskategori(Inntektskategori.UDEFINERT)
				.medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
				.medAndelNr(2L)
				.build();
		var andelSN = FordelAndelModell.builder()
				.medAktivitetStatus(AktivitetStatus.SN)
				.medInntektskategori(Inntektskategori.UDEFINERT)
				.medAndelNr(3L)
				.build();
		var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), TIDENES_ENDE), Arrays.asList(andelBA, andelSN, andelFL));

		// Act
		Evaluation evaluation = new SkalOmfordeleFraBrukersAndelTilFLEllerSN().evaluate(periode);

		// Assert
		assertThat(evaluation.result()).isEqualTo(Resultat.JA);
	}


	@Test
	void skal_ikkje_omfordele_fra_brukers_andel_med_kun_arbeidstaker() {
		// Arrange
		var andelBA = FordelAndelModell.builder()
				.medAktivitetStatus(AktivitetStatus.BA)
				.medForeslåttPrÅr(BigDecimal.valueOf(200_000))
				.medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
				.medAndelNr(1L)
				.build();
		var andelAT = FordelAndelModell.builder()
				.medAktivitetStatus(AktivitetStatus.AT)
				.medInntektskategori(Inntektskategori.UDEFINERT)
				.medArbeidsforhold(Arbeidsforhold.builder()
						.medAktivitet(Aktivitet.ARBEIDSTAKERINNTEKT)
						.medOrgnr("123423874")
						.build())
				.medAndelNr(2L)
				.build();
		var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), TIDENES_ENDE), Arrays.asList(andelBA, andelAT));

		// Act
		Evaluation evaluation = new SkalOmfordeleFraBrukersAndelTilFLEllerSN().evaluate(periode);

		// Assert
		assertThat(evaluation.result()).isEqualTo(Resultat.NEI);
	}


}