package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModellMellomregning;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RegelFordelBeregningsgrunnlagAndelsmessigTest {

	@Test
	public void et_foreslått_og_et_tilkommet_arbeidsforhold() {
		// Arrange
		FordelAndelModell foreslåttAndel = lagForeslåttAndel(AktivitetStatus.AT, Inntektskategori.ARBEIDSTAKER, arbeid("999", "abc"), 500000, 500000);
		FordelAndelModell tilkommetAndel = lagTilkommetAndel(AktivitetStatus.AT, Inntektskategori.ARBEIDSTAKER, arbeid("888", "abc"), 500000, 500000);

		// Act
		List<FordelAndelModell> fordelteAndeler = kjørRegel(foreslåttAndel, tilkommetAndel);

		// Assert
		assertThat(fordelteAndeler).hasSize(2);
		assertFordeltAndel(fordelteAndeler, arbeid("999", "abc"), Inntektskategori.ARBEIDSTAKER, 250000);
		assertFordeltAndel(fordelteAndeler, arbeid("888", "abc"), Inntektskategori.ARBEIDSTAKER,250000);
	}

	// Inntekt på STP, 300.000. Total ref: 500.000.
	// Fraksjoner: foreslåttAndel: 100000 / 500000 = 0.2 tilkommetAndel: 400000 / 500000 = 0.8
	// Fordelt: foreslåttAndel: 0.2 * 300000 = 60000 tilkommetAndel: 0.8 * 300000 = 240000
	@Test
	public void fordel_fra_at_og_sn_til_tilkommet() {
		// Arrange
		FordelAndelModell foreslåttAndel = lagForeslåttAndel(AktivitetStatus.AT, Inntektskategori.ARBEIDSTAKER, arbeid("999", "abc"), 100000, 100000);
		FordelAndelModell foreslåttSN = lagForeslåttAndel(AktivitetStatus.SN, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, null, 200000, null);
		FordelAndelModell tilkommetAndel = lagTilkommetAndel(AktivitetStatus.AT, Inntektskategori.ARBEIDSTAKER, arbeid("888", "abc"), 400000, 400000);

		// Act
		List<FordelAndelModell> fordelteAndeler = kjørRegel(foreslåttAndel, tilkommetAndel, foreslåttSN);

		// Assert
		assertThat(fordelteAndeler).hasSize(4);
		assertFordeltAndel(fordelteAndeler, arbeid("999", "abc"), Inntektskategori.ARBEIDSTAKER, 60000);
		assertFordeltAndel(fordelteAndeler, AktivitetStatus.SN, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, 0);
		assertFordeltAndel(fordelteAndeler, arbeid("888", "abc"), Inntektskategori.ARBEIDSTAKER,40000);
		assertFordeltAndel(fordelteAndeler, arbeid("888", "abc"), Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE,200000);
	}

	// Inntekt på STP, 200.000. Total ref: 400000.000.
	// Fraksjoner: tikommetAndel1: 100000 / 400000 = 0.25 tilkommetAndel2: 300000 / 400000 = 0.75
	// Fordelt: foreslåttAndel1: 0.25 * 200000 = 50000 tilkommetAndel2: 0.75 * 200000 = 150000
	@Test
	public void fordel_fra_dp_til_flere_tilkommet() {
		// Arrange
		FordelAndelModell foreslåttDP = lagForeslåttAndel(AktivitetStatus.DP, Inntektskategori.DAGPENGER, null, 200000, null);
		FordelAndelModell tilkommetAndel1 = lagTilkommetAndel(AktivitetStatus.AT, Inntektskategori.ARBEIDSTAKER, arbeid("888", "abc"), 100000, 100000);
		FordelAndelModell tilkommetAndel2 = lagTilkommetAndel(AktivitetStatus.AT, Inntektskategori.ARBEIDSTAKER, arbeid("777", "abc"), 300000, 300000);

		// Act
		List<FordelAndelModell> fordelteAndeler = kjørRegel(foreslåttDP, tilkommetAndel1, tilkommetAndel2);

		// Assert
		assertThat(fordelteAndeler).hasSize(3);
		assertFordeltAndel(fordelteAndeler, AktivitetStatus.DP, Inntektskategori.DAGPENGER, 0);
		assertFordeltAndel(fordelteAndeler, arbeid("888", "abc"), Inntektskategori.DAGPENGER,50000);
		assertFordeltAndel(fordelteAndeler, arbeid("777", "abc"), Inntektskategori.DAGPENGER,150000);
	}

	@Test
	public void fordel_fra_dp_fl_sn_til_en_tilkommet() {
		// Arrange
		FordelAndelModell foreslåttDP = lagForeslåttAndel(AktivitetStatus.DP, Inntektskategori.DAGPENGER, null, 100000, null);
		FordelAndelModell foreslåttSN = lagForeslåttAndel(AktivitetStatus.SN, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, null, 140000, null);
		FordelAndelModell foreslåttFL = lagForeslåttAndel(AktivitetStatus.FL, Inntektskategori.FRILANSER, frilans(), 277000, null);
		FordelAndelModell tilkommetAndel1 = lagTilkommetAndel(AktivitetStatus.AT, Inntektskategori.ARBEIDSTAKER, arbeid("999", "abc"), 500000, 500000);

		// Act
		List<FordelAndelModell> fordelteAndeler = kjørRegel(foreslåttDP, foreslåttFL, foreslåttSN, tilkommetAndel1);

		// Assert
		assertThat(fordelteAndeler).hasSize(6);
		assertFordeltAndel(fordelteAndeler, AktivitetStatus.DP, Inntektskategori.DAGPENGER, 0);
		assertFordeltAndel(fordelteAndeler, AktivitetStatus.FL, Inntektskategori.FRILANSER, 0);
		assertFordeltAndel(fordelteAndeler, AktivitetStatus.SN, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, 0);
		assertFordeltAndel(fordelteAndeler, arbeid("999", "abc"), Inntektskategori.DAGPENGER,100000);
		assertFordeltAndel(fordelteAndeler, arbeid("999", "abc"), Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE,140000);
		assertFordeltAndel(fordelteAndeler, arbeid("999", "abc"), Inntektskategori.FRILANSER,277000);
	}

	private void assertFordeltAndel(List<FordelAndelModell> fordelteAndeler, Arbeidsforhold arbeidsforhold, Inntektskategori forventetInntektskategori, Integer forventetFordeltBeløp) {
		FordelAndelModell match = fordelteAndeler.stream().filter(a -> a.getInntektskategori().equals(forventetInntektskategori) && Objects.equals(a.getArbeidsforhold().orElse(null), arbeidsforhold)).findFirst().orElse(null);
		assertThat(match).isNotNull();
		assertThat(match.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(forventetFordeltBeløp));
	}

	private void assertFordeltAndel(List<FordelAndelModell> fordelteAndeler, AktivitetStatus status, Inntektskategori forventetInntektskategori, Integer forventetFordeltBeløp) {
		FordelAndelModell match = fordelteAndeler.stream().filter(a -> a.getAktivitetStatus().equals(status)).findFirst().orElse(null);
		assertThat(match).isNotNull();
		assertThat(match.getInntektskategori()).isEqualByComparingTo(forventetInntektskategori);
		assertThat(match.getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(forventetFordeltBeløp));
	}

	private FordelAndelModell lagForeslåttAndel(AktivitetStatus status, Inntektskategori ik, Arbeidsforhold ag, Integer brutto, Integer refuson) {
		FordelAndelModell.Builder fordelAndel = FordelAndelModell.builder()
				.medAktivitetStatus(status)
				.medForeslåttPrÅr(BigDecimal.valueOf(brutto))
				.medInntektskategori(ik);
		if (refuson != null) {
			fordelAndel.medGjeldendeRefusjonPrÅr(BigDecimal.valueOf(refuson));
		}
		if (ag != null) {
			fordelAndel.medArbeidsforhold(ag);
		}
		return fordelAndel.build();
	}

	private FordelAndelModell lagTilkommetAndel(AktivitetStatus status, Inntektskategori ik, Arbeidsforhold ag, Integer inntektFraIM, Integer refuson) {
		FordelAndelModell.Builder fordelAndel = FordelAndelModell.builder()
				.medAktivitetStatus(status)
				.medInntektFraInnektsmelding(BigDecimal.valueOf(inntektFraIM))
				.medInntektskategori(ik);
		if (refuson != null) {
			fordelAndel.medGjeldendeRefusjonPrÅr(BigDecimal.valueOf(refuson));
		}
		if (ag != null) {
			fordelAndel.medArbeidsforhold(ag);
		}
		return fordelAndel.build();
	}

	private FordelAndelModell lagFordelAndelUtenArbeidsforhold(AktivitetStatus status, Inntektskategori ik, Integer brutto) {
		FordelAndelModell.Builder fordelAndel = FordelAndelModell.builder()
				.medAktivitetStatus(status)
				.medInntektskategori(ik);
		return brutto == null ? fordelAndel.build() : fordelAndel.medForeslåttPrÅr(BigDecimal.valueOf(brutto)).build();
	}

	private Arbeidsforhold arbeid(String orgnr, String arbeidsforholdId) {
		return Arbeidsforhold.nyttArbeidsforholdHosVirksomhet(orgnr, arbeidsforholdId);
	}

	private Arbeidsforhold frilans() {
		return Arbeidsforhold.frilansArbeidsforhold();
	}

	private List<FordelAndelModell> kjørRegel(FordelAndelModell... inputobjekter) {
		List<FordelAndelModell> inputAndeler = Arrays.asList(inputobjekter);
		FordelPeriodeModell fordelPeriode = new FordelPeriodeModell(Periode.of(LocalDate.now(), LocalDate.now().plusDays(1)), inputAndeler);
		FordelModell modell = new FordelModell(fordelPeriode);
		new RegelFordelBeregningsgrunnlagAndelsmessig().getSpecification().evaluate(modell);
		return modell.getMellomregninger().stream()
				.map(FordelAndelModellMellomregning::getFordelteAndeler)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}
}