package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelteAndelerModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.fpsak.tidsserie.LocalDateInterval;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class FinnMålbeløpForFordelingenPrAndelTest {

	@Test
	public void skal_sette_målbeløp_for_to_andeler_der_et_er_tilkommet() {
		// Arrange
		FordelteAndelerModell foreslåttAndel = new FordelteAndelerModell(lagFordelAndelMedForeslått(AktivitetStatus.AT, arbeid("999", "abc"), 500_000, 300_000));
		FordelteAndelerModell tilkommetAndel = new FordelteAndelerModell(lagFordelAndelTilkommet(AktivitetStatus.AT, arbeid("888", "abc"), 500_000, 500_000));
		foreslåttAndel.setFraksjonAvBrutto(BigDecimal.valueOf(0.375));
		tilkommetAndel.setFraksjonAvBrutto(BigDecimal.valueOf(0.625));

		// Act
		kjørRegel(foreslåttAndel, tilkommetAndel);

		// Assert
		assertThat(foreslåttAndel.getMålbeløp()).isEqualByComparingTo(BigDecimal.valueOf(187_500));
		assertThat(tilkommetAndel.getMålbeløp()).isEqualByComparingTo(BigDecimal.valueOf(312_500));
	}

	private void kjørRegel(FordelteAndelerModell... mellom) {
		List<FordelteAndelerModell> mellomregninger = Arrays.asList(mellom);
		List<FordelAndelModell> inputAndeler = mellomregninger.stream().map(FordelteAndelerModell::getInputAndel).collect(Collectors.toList());
		FordelPeriodeModell periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), LocalDateInterval.TIDENES_ENDE), inputAndeler);
		FordelModell modell = new FordelModell(periode);
		mellomregninger.forEach(modell::leggTilMellomregningAndel);
		new FinnMålbeløpForFordelingenPrAndel().evaluate(modell);
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
			fordelAndel.medInntektFraInnektsmelding(BigDecimal.valueOf(inntektFraIM));
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