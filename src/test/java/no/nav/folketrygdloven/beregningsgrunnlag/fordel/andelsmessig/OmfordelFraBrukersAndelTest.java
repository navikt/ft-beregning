package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;


import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;

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