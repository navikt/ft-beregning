package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelteAndelerModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.fpsak.tidsserie.LocalDateInterval;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FinnFraksjonPrAndelTest {


	@Test
	public void skal_teste_to_andeler_lik_inntekt_og_refusjon() {
		// Arrange
		FordelAndelModell foreslåttAndel = lagFordelAndelMedForeslått(AktivitetStatus.AT, arbeid("999", "abc"), 500_000, 500_000);
		FordelAndelModell tilkommet = lagFordelAndelTilkommet(AktivitetStatus.AT, arbeid("888", "abc"), 500_000, 500_000);

		// Act
		FordelModell fordelModell = kjørRegel(foreslåttAndel, tilkommet);

		// Assert
		assertThat(fordelModell.getMellomregninger()).hasSize(2);
		assertAndel(fordelModell, foreslåttAndel, 0.5);
		assertAndel(fordelModell, tilkommet, 0.5);
	}

	@Test
	public void skal_teste_to_andeler_en_krever_mindre_refusjon_enn_den_har_i_brutto() {
		// Arrange
		FordelAndelModell foreslåttAndel = lagFordelAndelMedForeslått(AktivitetStatus.AT, arbeid("999", "abc"), 500_000, 300_000);
		FordelAndelModell tilkommet = lagFordelAndelTilkommet(AktivitetStatus.AT, arbeid("888", "abc"), 500_000, 500_000);

		// Act
		FordelModell fordelModell = kjørRegel(foreslåttAndel, tilkommet);

		// Assert
		assertThat(fordelModell.getMellomregninger()).hasSize(2);
		assertAndel(fordelModell, foreslåttAndel, 0.375);
		assertAndel(fordelModell, tilkommet, 0.625);
	}

	/**
	 * Her er brutto ved stp 600.000, men refusjon blir 800.000 med tilkommet arbeidsforhold
	 * Andeler som krever refusjon har 750.000 i fraksjonsbestemmende beløp
	 */
	@Test
	public void skal_teste_fem_andeler_der_enkelte_ikke_krever_refusjon() {
		// Arrange
		FordelAndelModell foreslåttAndel = lagFordelAndelMedForeslått(AktivitetStatus.AT, arbeid("999", "abc"), 300_000, 400_000);
		FordelAndelModell foreslåttAndelUtenRef = lagFordelAndelMedForeslått(AktivitetStatus.AT, arbeid("888", "abc"), 200_000, 0);
		FordelAndelModell tilkommetAndelMedRef = lagFordelAndelTilkommet(AktivitetStatus.AT, arbeid("777", "abc"), 450_000, 450_000);
		FordelAndelModell næringsandel = lagFordelAndelForStatus(AktivitetStatus.SN, 50_000);
		FordelAndelModell dagpengeAndel = lagFordelAndelForStatus(AktivitetStatus.DP, 50_000);


		// Act
		FordelModell fordelModell = kjørRegel(foreslåttAndel, foreslåttAndelUtenRef, tilkommetAndelMedRef, næringsandel, dagpengeAndel);

		// Assert
		assertThat(fordelModell.getMellomregninger()).hasSize(5);
		assertAndel(fordelModell, foreslåttAndel, 0.4);
		assertAndel(fordelModell, foreslåttAndelUtenRef, 0);
		assertAndel(fordelModell, tilkommetAndelMedRef, 0.6);
		assertAndel(fordelModell, næringsandel, 0);
		assertAndel(fordelModell, dagpengeAndel, 0);
	}

	@Test
	public void skal_teste_at_refusjonsbeløp_brukes_til_å_bestemme_fraksjon_om_mindre_enn_inntekt() {
		// Arrange
		FordelAndelModell foreslåttAndel = lagFordelAndelMedForeslått(AktivitetStatus.AT, arbeid("999", "abc"), 100_000, 100_000);
		FordelAndelModell foreslåttAndelUtenRef = lagFordelAndelMedForeslått(AktivitetStatus.AT, arbeid("888", "abc"), 200_000, 0);
		FordelAndelModell tilkommetAndelMedRef = lagFordelAndelTilkommet(AktivitetStatus.AT, arbeid("777", "abc"), 400_000, 300_000);


		// Act
		FordelModell fordelModell = kjørRegel(foreslåttAndel, foreslåttAndelUtenRef, tilkommetAndelMedRef);

		// Assert
		assertThat(fordelModell.getMellomregninger()).hasSize(3);
		assertAndel(fordelModell, foreslåttAndel, 0.25);
		assertAndel(fordelModell, foreslåttAndelUtenRef, 0);
		assertAndel(fordelModell, tilkommetAndelMedRef, 0.75);
	}

	@Test
	public void skal_teste_tre_andeler_som_ikke_kan_deles_perfekt_på_tre() {
		// Arrange
		FordelAndelModell foreslåttAndel1 = lagFordelAndelMedForeslått(AktivitetStatus.AT, arbeid("999", "abc"), 586_104, 586_104);
		FordelAndelModell foreslåttAndel2 = lagFordelAndelTilkommet(AktivitetStatus.AT, arbeid("888", "abc"), 624_996, 624_996);
		FordelAndelModell tilkommetAndel = lagFordelAndelTilkommet(AktivitetStatus.AT, arbeid("777", "abc"), 624_996, 500_004);


		// Act
		FordelModell fordelModell = kjørRegel(foreslåttAndel1, foreslåttAndel2, tilkommetAndel);

		// Assert
		assertThat(fordelModell.getMellomregninger()).hasSize(3);
		assertAndel(fordelModell, foreslåttAndel1, 0.3425297352);
		assertAndel(fordelModell, foreslåttAndel2, 0.3652589206);
		assertAndel(fordelModell, tilkommetAndel, 0.2922113442);
	}

	private void assertAndel(FordelModell fordelModell, FordelAndelModell andel, double fraksjon) {
		Optional<FordelteAndelerModell> match = fordelModell.getMellomregninger().stream().filter(a -> a.getInputAndel().equals(andel)).findFirst();
		assertThat(match).isPresent();
		assertThat(match.get().getFraksjonAvBrutto()).isEqualByComparingTo(BigDecimal.valueOf(fraksjon));
	}

	private FordelModell kjørRegel(FordelAndelModell... andeler) {
		List<FordelAndelModell> andelerInput = Arrays.asList(andeler);
		FordelPeriodeModell periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), LocalDateInterval.TIDENES_ENDE), andelerInput);
		FordelModell modell = new FordelModell(periode);
		new FinnFraksjonPrAndel().evaluate(modell);
		return modell;
	}

	private Arbeidsforhold arbeid(String orgnr, String arbeidsforholdId) {
		return Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr, arbeidsforholdId);
	}

	private FordelAndelModell lagFordelAndelTilkommet(AktivitetStatus status, Arbeidsforhold ag, Integer inntektFraIM, Integer refusjon) {
		return lagFordelAndelMedArbeidsforhold(status, ag, null, inntektFraIM, refusjon);
	}

	private FordelAndelModell lagFordelAndelMedForeslått(AktivitetStatus status, Arbeidsforhold ag, Integer foreslått, Integer refusjon) {
		return lagFordelAndelMedArbeidsforhold(status, ag, foreslått, null, refusjon);
	}

	private FordelAndelModell lagFordelAndelMedArbeidsforhold(AktivitetStatus status, Arbeidsforhold ag, Integer brutto, Integer inntektFraIM, Integer refusjon) {
		FordelAndelModell.Builder fordelAndel = FordelAndelModell.builder()
				.medAktivitetStatus(status)
				.medArbeidsforhold(ag);
		if (inntektFraIM != null) {
			fordelAndel.medInntektFraInnektsmelding(BigDecimal.valueOf(inntektFraIM).divide(BigDecimal.valueOf(12), RoundingMode.HALF_EVEN));
		} else {
			fordelAndel.medForeslåttPrÅr(BigDecimal.valueOf(brutto));
		}
		if (refusjon != null) {
			fordelAndel.medGjeldendeRefusjonPrÅr(BigDecimal.valueOf(refusjon));
		}
		return fordelAndel.build();
	}

	private FordelAndelModell lagFordelAndelForStatus(AktivitetStatus status, Integer brutto) {
		FordelAndelModell.Builder fordelAndel = FordelAndelModell.builder()
				.medAktivitetStatus(status)
				.medForeslåttPrÅr(BigDecimal.valueOf(brutto));
		return fordelAndel.build();
	}

}