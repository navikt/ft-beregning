package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static no.nav.folketrygdloven.beregningsgrunnlag.util.DateUtil.TIDENES_ENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

class OmfordelFraBrukersAndelTest {

	@Test
	void skal_omfordele_fra_brukers_andel_til_frilans() {
		// Arrange
		BigDecimal inntekt = BigDecimal.valueOf(200_000);
		Inntektskategori arbeidstakerUtenFeriepenger = Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
		BeregningsgrunnlagPrStatus brukers_andel = lagBrukersAndel(inntekt, arbeidstakerUtenFeriepenger, 1L);
		BeregningsgrunnlagPrArbeidsforhold frilans = lagFrilans(2L);
		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(brukers_andel)
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.ATFL)
						.medArbeidsforhold(frilans)
						.build())
				.build();

		kjørRegel(periode);

		// Regelen endrer på input så vi kan asserte på brukers_andel og frilans
		assertThat(brukers_andel.getFordeltPrÅr()).isEqualTo(BigDecimal.ZERO);
		assertThat(frilans.getFordeltPrÅr()).isEqualTo(inntekt);
		assertThat(frilans.getInntektskategori()).isEqualTo(arbeidstakerUtenFeriepenger);
	}

	@Test
	void skal_omfordele_fra_brukers_andel_til_sn() {
		// Arrange
		BigDecimal inntekt = BigDecimal.valueOf(200_000);
		Inntektskategori arbeidstakerUtenFeriepenger = Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
		BeregningsgrunnlagPrStatus brukers_andel = lagBrukersAndel(inntekt, arbeidstakerUtenFeriepenger, 1L);

		long andelsnrSN = 2L;
		BeregningsgrunnlagPrStatus snStatus = lagSN(andelsnrSN);
		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(brukers_andel)
				.medBeregningsgrunnlagPrStatus(snStatus)
				.build();

		kjørRegel(periode);

		// Regelen endrer på input så vi kan asserte på brukers_andel og sn
		assertThat(brukers_andel.getFordeltPrÅr()).isEqualTo(BigDecimal.ZERO);
		assertThat(snStatus.getFordeltPrÅr()).isEqualTo(inntekt);
		assertThat(snStatus.getInntektskategori()).isEqualTo(arbeidstakerUtenFeriepenger);
	}


	@Test
	void skal_omfordele_fra_brukers_andel_til_frilans_for_periode_med_fl_og_sn() {
		// Arrange
		BigDecimal inntekt = BigDecimal.valueOf(200_000);
		Inntektskategori arbeidstakerUtenFeriepenger = Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
		BeregningsgrunnlagPrStatus brukers_andel = lagBrukersAndel(inntekt, arbeidstakerUtenFeriepenger, 1L);
		long andelsnrFrilans = 2L;
		BeregningsgrunnlagPrArbeidsforhold frilans = lagFrilans(andelsnrFrilans);
		long andelsnrSN = 3L;
		BeregningsgrunnlagPrStatus snStatus = lagSN(andelsnrSN);
		BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
				.medPeriode(Periode.of(LocalDate.now(), TIDENES_ENDE))
				.medBeregningsgrunnlagPrStatus(brukers_andel)
				.medBeregningsgrunnlagPrStatus(snStatus)
				.medBeregningsgrunnlagPrStatus(BeregningsgrunnlagPrStatus.builder()
						.medAktivitetStatus(AktivitetStatus.ATFL)
						.medArbeidsforhold(frilans)
						.build())
				.build();

		kjørRegel(periode);

		// Regelen endrer på input så vi kan asserte på brukers_andel og frilans/sn
		assertThat(brukers_andel.getFordeltPrÅr()).isEqualTo(BigDecimal.ZERO);
		assertThat(frilans.getFordeltPrÅr()).isEqualTo(inntekt);
		assertThat(frilans.getInntektskategori()).isEqualTo(arbeidstakerUtenFeriepenger);
		assertThat(snStatus.getBruttoPrÅr()).isEqualTo(BigDecimal.ZERO);
	}

	private BeregningsgrunnlagPrStatus lagSN(long andelsnrSN) {
		return BeregningsgrunnlagPrStatus.builder()
				.medAktivitetStatus(AktivitetStatus.SN)
				.medInntektskategori(Inntektskategori.UDEFINERT)
				.medAndelNr(andelsnrSN)
				.build();
	}

	private BeregningsgrunnlagPrArbeidsforhold lagFrilans(long andelsnrFrilans) {
		return BeregningsgrunnlagPrArbeidsforhold.builder()
				.medInntektskategori(Inntektskategori.UDEFINERT)
				.medArbeidsforhold(Arbeidsforhold.builder()
						.medAktivitet(Aktivitet.FRILANSINNTEKT)
						.build())
				.medAndelNr(andelsnrFrilans)
				.build();
	}


	private BeregningsgrunnlagPrStatus lagBrukersAndel(BigDecimal inntekt, Inntektskategori arbeidstakerUtenFeriepenger, long andelsnrBrukersAndel) {
		BeregningsgrunnlagPrStatus brukers_andel = BeregningsgrunnlagPrStatus.builder()
				.medAktivitetStatus(AktivitetStatus.BA)
				.medBeregnetPrÅr(inntekt)
				.medInntektskategori(arbeidstakerUtenFeriepenger)
				.medAndelNr(andelsnrBrukersAndel)
				.build();
		return brukers_andel;
	}

	private void kjørRegel(BeregningsgrunnlagPeriode periode) {
		new OmfordelFraBrukersAndel().evaluate(periode);
	}
}