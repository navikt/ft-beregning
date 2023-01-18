package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;

class SettAndelerUtenSøktYtelseTilNullTest {

	public static final Arbeidsforhold ARBEIDSFORHOLD = Arbeidsforhold.nyttArbeidsforholdHosVirksomhet("123456789");


	@Test
	void skal_ikke_sette_til_null_for_eksisterende_andel() {
		var andel = lagArbeidsforhold(BigDecimal.valueOf(500_000), null, 1L, 0, AktivitetStatus.AT);
		var andeler = List.of(andel);

		kjørRegel(andeler);

		assertThat(andel.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
		assertThat(andel.getFordeltPrÅr()).isEmpty();
	}

	@Test
	void skal_ikke_sette_til_null_for_andel_med_utbetalingsgrad() {
		var andel = lagArbeidsforhold(null, null, 1L, 1, AktivitetStatus.AT);
		var andeler = List.of(andel);

		kjørRegel(andeler);

		assertThat(andel.getInntektskategori()).isEqualTo(null);
		assertThat(andel.getFordeltPrÅr()).isEmpty();
	}

	@Test
	void skal_ikke_sette_til_null_for_andel_som_allerede_er_fordelt() {
		var andel = lagArbeidsforhold(null, BigDecimal.valueOf(100_000), 1L, 0, AktivitetStatus.AT);
		var andeler = List.of(andel);

		kjørRegel(andeler);

		assertThat(andel.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
		assertThat(andel.getFordeltPrÅr().get().compareTo(BigDecimal.valueOf(100_000)) == 0).isTrue();
	}

	@Test
	void skal_sette_til_null_med_inntektskategori_arbeidstaker() {
		var andel = lagArbeidsforhold(null, null, 1L, 0, AktivitetStatus.AT);
		var andeler = List.of(andel);

		kjørRegel(andeler);

		assertThat(andel.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
		assertThat(andel.getFordeltPrÅr().get().compareTo(BigDecimal.ZERO) == 0).isTrue();
	}

	@Test
	void skal_sette_til_null_med_inntektskategori_selvstendig_næringsdrivende() {
		var andel = lagArbeidsforhold(null, null, 1L, 0, AktivitetStatus.SN);
		var andeler = List.of(andel);

		kjørRegel(andeler);

		assertThat(andel.getInntektskategori()).isEqualTo(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
		assertThat(andel.getFordeltPrÅr().get().compareTo(BigDecimal.ZERO) == 0).isTrue();
	}

	@Test
	void skal_sette_til_null_med_inntektskategori_frilanser() {
		var andel = lagArbeidsforhold(null, null, 1L, 0, AktivitetStatus.FL);
		var andeler = List.of(andel);

		kjørRegel(andeler);

		assertThat(andel.getInntektskategori()).isEqualTo(Inntektskategori.FRILANSER);
		assertThat(andel.getFordeltPrÅr().get().compareTo(BigDecimal.ZERO) == 0).isTrue();
	}


	@Test
	void skal_sette_til_null_med_inntektskategori_dagpenger() {
		var andel = lagArbeidsforhold(null, null, 1L, 0, AktivitetStatus.DP);
		var andeler = List.of(andel);

		kjørRegel(andeler);

		assertThat(andel.getInntektskategori()).isEqualTo(Inntektskategori.DAGPENGER);
		assertThat(andel.getFordeltPrÅr().get().compareTo(BigDecimal.ZERO) == 0).isTrue();
	}

	@Test
	void skal_sette_til_null_med_inntektskategori_aap() {
		var andel = lagArbeidsforhold(null, null, 1L, 0, AktivitetStatus.AAP);
		var andeler = List.of(andel);

		kjørRegel(andeler);

		assertThat(andel.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSAVKLARINGSPENGER);
		assertThat(andel.getFordeltPrÅr().get().compareTo(BigDecimal.ZERO) == 0).isTrue();
	}

	@Test
	void skal_sette_til_null_med_inntektskategori_arbeidstaker_uten_feriepenger() {
		var andel = lagArbeidsforhold(null, null, 1L, 0, AktivitetStatus.BA);
		var andeler = List.of(andel);

		kjørRegel(andeler);

		assertThat(andel.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER);
		assertThat(andel.getFordeltPrÅr().get().compareTo(BigDecimal.ZERO) == 0).isTrue();
	}


	private void kjørRegel(List<FordelAndelModell> andeler) {
		var modell = lagModell(andeler);
		new SettAndelerUtenSøktYtelseTilNull().evaluate(modell);
	}

	private FordelModell lagModell(List<FordelAndelModell> andeler) {
		return new FordelModell(new FordelPeriodeModell(andeler));
	}


	private FordelAndelModell lagArbeidsforhold(BigDecimal brutto, BigDecimal fordelt, Long andelsnr, int utbetalingsgrad, AktivitetStatus aktivitetStatus) {
		return FordelAndelModell.builder()
				.medAndelNr(andelsnr)
				.medAktivitetStatus(aktivitetStatus)
				.medForeslåttPrÅr(brutto)
				.medFordeltPrÅr(fordelt)
				.medInntektskategori(brutto != null || fordelt != null ? Inntektskategori.ARBEIDSTAKER : null)
				.medArbeidsforhold(lagArbeidsforhold(aktivitetStatus))
				.medUtbetalingsgrad(BigDecimal.valueOf(utbetalingsgrad))
				.build();
	}

	private Arbeidsforhold lagArbeidsforhold(AktivitetStatus aktivitetStatus) {
		if (aktivitetStatus.equals(AktivitetStatus.AT)) {
			return ARBEIDSFORHOLD;
		} else if (aktivitetStatus.equals(AktivitetStatus.FL)) {
			return Arbeidsforhold.frilansArbeidsforhold();
		}
		return null;
	}
}