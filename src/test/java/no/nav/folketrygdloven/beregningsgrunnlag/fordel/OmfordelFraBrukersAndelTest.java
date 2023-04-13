package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;


import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;

class OmfordelFraBrukersAndelTest {

	@Test
	void skal_omfordele_fra_brukers_andel_til_frilans() {
		// Arrange
		var inntekt = BigDecimal.valueOf(200_000);
		var arbeidstakerUtenFeriepenger = Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
		var brukers_andel = lagBrukersAndel(inntekt, arbeidstakerUtenFeriepenger, 1L);
		var frilans = lagFrilans(2L);

		var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), TIDENES_ENDE), Arrays.asList(brukers_andel, frilans));
		FordelModell regelmodell = new FordelModell(periode);
		kjørRegel(regelmodell);

		// Regelen endrer på input så vi kan asserte på brukers_andel og frilans
		assertThat(brukers_andel.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(frilans.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(inntekt);
		assertThat(frilans.getInntektskategori()).isEqualTo(arbeidstakerUtenFeriepenger);
	}

	@Test
	void skal_omfordele_fra_to_brukers_andel_til_frilans() {
		// Arrange
		var inntekt = BigDecimal.valueOf(200_000);
		var inntekt2 = BigDecimal.valueOf(40_000);
		var brukers_andel = lagBrukersAndel(inntekt, Inntektskategori.ARBEIDSTAKER, 1L);
		var brukers_andel2 = lagBrukersAndel(inntekt2, Inntektskategori.FRILANSER, 2L);
		var frilans = lagFrilans(3L);

		var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), TIDENES_ENDE), Arrays.asList(brukers_andel, brukers_andel2, frilans));
		FordelModell regelmodell = new FordelModell(periode);
		kjørRegel(regelmodell);

		// Regelen endrer på input så vi kan asserte på brukers_andel og frilans
		assertThat(periode.getAndeler()).hasSize(4);
		assertThat(brukers_andel.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(brukers_andel2.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(frilans.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(inntekt);
		assertThat(frilans.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
		var nyAndel = periode.getAndeler().get(3);
		assertThat(nyAndel.getInntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
		assertThat(nyAndel.getFordeltPrÅr().orElseThrow()).isEqualTo(inntekt2);
	}

	@Test
	void skal_omfordele_fra_brukers_andel_til_sn() {
		// Arrange
		var inntekt = BigDecimal.valueOf(200_000);
		var arbeidstakerUtenFeriepenger = Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
		var brukers_andel = lagBrukersAndel(inntekt, arbeidstakerUtenFeriepenger, 1L);

		long andelsnrSN = 2L;
		var snStatus = lagSN(andelsnrSN);

		var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), TIDENES_ENDE), Arrays.asList(brukers_andel, snStatus));

		FordelModell regelmodell = new FordelModell(periode);
		kjørRegel(regelmodell);

		// Regelen endrer på input så vi kan asserte på brukers_andel og sn
		assertThat(brukers_andel.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(snStatus.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(inntekt);
		assertThat(snStatus.getInntektskategori()).isEqualTo(arbeidstakerUtenFeriepenger);
	}

	@Test
	void skal_omfordele_fra_to_brukers_andel_til_sn() {
		// Arrange
		var inntekt = BigDecimal.valueOf(200_000);
		var inntekt2 = BigDecimal.valueOf(100_000);
		var brukers_andel = lagBrukersAndel(inntekt, Inntektskategori.ARBEIDSTAKER, 1L);
		var brukers_andel2 = lagBrukersAndel(inntekt2, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, 2L);

		long andelsnrSN = 3L;
		var snStatus = lagSN(andelsnrSN);

		var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), TIDENES_ENDE), Arrays.asList(brukers_andel,brukers_andel2, snStatus));

		FordelModell regelmodell = new FordelModell(periode);
		kjørRegel(regelmodell);

		// Regelen endrer på input så vi kan asserte på brukers_andel og sn
		assertThat(periode.getAndeler()).hasSize(4);
		assertThat(brukers_andel.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(brukers_andel2.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(snStatus.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(inntekt);
		assertThat(snStatus.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
		var nyAndel = periode.getAndeler().get(3);
		assertThat(nyAndel.getAktivitetStatus()).isEqualTo(AktivitetStatus.SN);
		assertThat(nyAndel.getInntektskategori()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
		assertThat(nyAndel.getFordeltPrÅr().orElseThrow()).isEqualTo(inntekt2);
	}


	@Test
	void skal_omfordele_fra_brukers_andel_til_frilans_for_periode_med_fl_og_sn() {
		// Arrange
		var inntekt = BigDecimal.valueOf(200_000);
		var arbeidstakerUtenFeriepenger = Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
		var brukers_andel = lagBrukersAndel(inntekt, arbeidstakerUtenFeriepenger, 1L);
		long andelsnrFrilans = 2L;
		var frilans = lagFrilans(andelsnrFrilans);
		long andelsnrSN = 3L;
		var snStatus = lagSN(andelsnrSN);
		var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), TIDENES_ENDE), Arrays.asList(brukers_andel, snStatus, frilans));
		FordelModell regelmodell = new FordelModell(periode);
		kjørRegel(regelmodell);

		// Regelen endrer på input så vi kan asserte på brukers_andel og frilans/sn
		assertThat(brukers_andel.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(frilans.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(inntekt);
		assertThat(frilans.getInntektskategori()).isEqualTo(arbeidstakerUtenFeriepenger);
		assertThat(snStatus.getBruttoPrÅr()).isEmpty();
	}

	private FordelAndelModell lagSN(long andelsnrSN) {
		return FordelAndelModell.builder()
				.medAktivitetStatus(AktivitetStatus.SN)
				.medInntektskategori(Inntektskategori.UDEFINERT)
				.medAndelNr(andelsnrSN)
				.build();
	}

	private FordelAndelModell lagFrilans(long andelsnrFrilans) {
		return FordelAndelModell.builder()
				.medInntektskategori(Inntektskategori.UDEFINERT)
				.medAktivitetStatus(AktivitetStatus.FL)
				.medArbeidsforhold(Arbeidsforhold.frilansArbeidsforhold())
				.medAndelNr(andelsnrFrilans)
				.build();
	}


	private FordelAndelModell lagBrukersAndel(BigDecimal inntekt, Inntektskategori arbeidstakerUtenFeriepenger, long andelsnrBrukersAndel) {
		return FordelAndelModell.builder()
				.medAktivitetStatus(AktivitetStatus.BA)
				.medForeslåttPrÅr(inntekt)
				.medInntektskategori(arbeidstakerUtenFeriepenger)
				.medAndelNr(andelsnrBrukersAndel)
				.build();
	}

	private void kjørRegel(FordelModell modell) {
		new OmfordelFraBrukersAndel().evaluate(modell);
	}
}