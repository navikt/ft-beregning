package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelteAndelerModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;

import no.nav.fpsak.tidsserie.LocalDateInterval;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class FordelMålbeløpPrAndelTest {

	@Test
	public void skal_teste_fordeling_mellom_arbeidsforhold_med_refusjon_med_samme_inntektskategori() {
		// Arrange
		FordelteAndelerModell andel1 = lagArbeidsandel(arbeid("999", "abc"), 200_000, 150_000);
		FordelteAndelerModell andel2 = lagArbeidsandel(arbeid("888", "abc"), null, 50_000);

		// Act
		FordelModell regelModell = kjørRegel(andel1, andel2);

		// Assert
		assertThat(regelModell.getMellomregninger()).hasSize(2);

		assertAntallFordelteAndelerResultat(regelModell, andel1, 1);
		assertFordeltAndel(regelModell, andel1, 150000, Inntektskategori.ARBEIDSTAKER);

		assertAntallFordelteAndelerResultat(regelModell, andel2, 1);
		assertFordeltAndel(regelModell, andel2, 50000, Inntektskategori.ARBEIDSTAKER);
	}

	@Test
	public void skal_teste_fordeling_fra_en_eksisterende_med_ref_til_to_tilkomne() {
		// Arrange
		FordelteAndelerModell andel1 = lagArbeidsandel(arbeid("999", "abc"), 200_000, 150_000);
		FordelteAndelerModell andel2 = lagArbeidsandel(arbeid("888", "abc"), null, 20_000);
		FordelteAndelerModell andel3 = lagArbeidsandel(arbeid("777", "abc"), null, 30_000);

		// Act
		FordelModell regelModell = kjørRegel(andel1, andel2, andel3);

		// Assert
		assertThat(regelModell.getMellomregninger()).hasSize(3);

		assertAntallFordelteAndelerResultat(regelModell, andel1, 1);
		assertFordeltAndel(regelModell, andel1, 150000, Inntektskategori.ARBEIDSTAKER);

		assertAntallFordelteAndelerResultat(regelModell, andel2, 1);
		assertFordeltAndel(regelModell, andel2, 20000, Inntektskategori.ARBEIDSTAKER);

		assertAntallFordelteAndelerResultat(regelModell, andel3, 1);
		assertFordeltAndel(regelModell, andel3, 30000, Inntektskategori.ARBEIDSTAKER);
	}

	@Test
	public void skal_overskrive_andel_med_inntektskategori_udefinert() {
		// Arrange
		FordelteAndelerModell andel1 = lagArbeidsandel(arbeid("999", "abc"), 400_000, 150_000);
		FordelteAndelerModell andel2 = lagArbeidsandelUdefinertIK(arbeid("888", "abc"), null, 250_000);

		// Act
		FordelModell regelModell = kjørRegel(andel1, andel2);

		// Assert
		assertThat(regelModell.getMellomregninger()).hasSize(2);

		assertAntallFordelteAndelerResultat(regelModell, andel1, 1);
		assertFordeltAndel(regelModell, andel1, 150000, Inntektskategori.ARBEIDSTAKER, false);

		assertAntallFordelteAndelerResultat(regelModell, andel2, 1);
		assertFordeltAndel(regelModell, andel2, 250000, Inntektskategori.ARBEIDSTAKER, false);
	}

	@Test
	public void skal_teste_fordeling_fra_næring_og_arbeid_til_to_tilkomne_likt_fordelt() {
		// Arrange
		FordelteAndelerModell atAndel = lagArbeidsandel(arbeid("999", "abc"), 60_000, 60_000);
		FordelteAndelerModell snAndel = lagSNAndel(100_000, 0);
		FordelteAndelerModell tilkommetAT1 = lagArbeidsandel(arbeid("888", "abc"), null, 45_000);
		FordelteAndelerModell tilkommetAT2 = lagArbeidsandel(arbeid("777", "abc"), null, 55_000);

		// Act
		FordelModell regelModell = kjørRegel(atAndel, snAndel, tilkommetAT1, tilkommetAT2);

		// Assert
		assertThat(regelModell.getMellomregninger()).hasSize(4);

		assertAntallFordelteAndelerResultat(regelModell, atAndel, 1);
		assertFordeltAndel(regelModell, atAndel, 60000, Inntektskategori.ARBEIDSTAKER, false);

		assertAntallFordelteAndelerResultat(regelModell, tilkommetAT1, 1);
		assertFordeltAndel(regelModell, tilkommetAT1, 45000, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, true);

		assertAntallFordelteAndelerResultat(regelModell, tilkommetAT2, 1);
		assertFordeltAndel(regelModell, tilkommetAT2, 55000, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, true);

		assertAntallFordelteAndelerResultat(regelModell, snAndel, 1);
		assertFordeltAndel(regelModell, snAndel, 0, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, false);
	}

	@Test
	public void skal_fordele_fra_tre_eksisterende_til_et_tilkommet_arbeidsforhold() {
		// Arrange
		FordelteAndelerModell andel1 = lagArbeidsandel(arbeid("999", "abc"), 200_000, 50_000);
		FordelteAndelerModell andel2 = lagArbeidsandel(arbeid("888", "abc"), 100_000, 100_000);
		FordelteAndelerModell andel3 = lagArbeidsandel(arbeid("777", "abc"), 50_000, 0);
		FordelteAndelerModell andel4 = lagArbeidsandel(arbeid("666", "abc"), null, 200_000);

		// Act
		FordelModell regelModell = kjørRegel(andel1, andel2, andel3, andel4);

		// Assert
		assertThat(regelModell.getMellomregninger()).hasSize(4);

		assertAntallFordelteAndelerResultat(regelModell, andel1, 1);
		assertFordeltAndel(regelModell, andel1, 50_000, Inntektskategori.ARBEIDSTAKER);

		assertAntallFordelteAndelerResultat(regelModell, andel2, 1);
		assertFordeltAndel(regelModell, andel2, 100_000, Inntektskategori.ARBEIDSTAKER);

		assertAntallFordelteAndelerResultat(regelModell, andel3, 1);
		assertFordeltAndel(regelModell, andel3, 0, Inntektskategori.ARBEIDSTAKER);

		assertAntallFordelteAndelerResultat(regelModell, andel4, 1);
		assertFordeltAndel(regelModell, andel4, 200_000, Inntektskategori.ARBEIDSTAKER);
	}

	@Test
	public void skal_fordele_fra_frilans_og_næring_og_arbeid_til_tilkommet_arbeid() {
		// Arrange
		FordelteAndelerModell arbeid = lagArbeidsandel(arbeid("999", "abc"), 200_000, 150_000);
		FordelteAndelerModell frilans = lagFLAndel(100_000, 0);
		FordelteAndelerModell næring = lagSNAndel(300_000, 0);
		FordelteAndelerModell tilkommetArbeid = lagArbeidsandel(arbeid("888", "abc"), null, 450_000);

		// Act
		FordelModell regelModell = kjørRegel(arbeid, frilans, næring, tilkommetArbeid);

		// Assert
		assertThat(regelModell.getMellomregninger()).hasSize(4);

		assertAntallFordelteAndelerResultat(regelModell, arbeid, 1);
		assertFordeltAndel(regelModell, arbeid, 150_000, Inntektskategori.ARBEIDSTAKER);

		assertAntallFordelteAndelerResultat(regelModell, frilans, 1);
		assertFordeltAndel(regelModell, frilans, 0, Inntektskategori.FRILANSER);

		assertAntallFordelteAndelerResultat(regelModell, næring, 1);
		assertFordeltAndel(regelModell, næring, 0, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);

		assertAntallFordelteAndelerResultat(regelModell, tilkommetArbeid, 3);
		assertFordeltAndel(regelModell, tilkommetArbeid, 50_000, Inntektskategori.ARBEIDSTAKER);
		assertFordeltAndel(regelModell, tilkommetArbeid, 100_000, Inntektskategori.FRILANSER);
		assertFordeltAndel(regelModell, tilkommetArbeid, 300_000, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
	}


	private void assertFordeltAndel(FordelModell regelModell, FordelteAndelerModell andel, int forventetFordelt, Inntektskategori forventetKategori) {
		FordelteAndelerModell resultat = getResultat(regelModell.getMellomregninger(), andel);
		Optional<FordelAndelModell> matchendeAndel = resultat.getFordelteAndeler().stream()
				.filter(res -> res.getInntektskategori().equals(forventetKategori))
				.findFirst();
		assertThat(matchendeAndel).isPresent();
		assertThat(matchendeAndel.get().getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(forventetFordelt));
	}

	private void assertFordeltAndel(FordelModell regelModell, FordelteAndelerModell andel, int forventetFordelt, Inntektskategori forventetKategori, boolean erNytt) {
		FordelteAndelerModell resultat = getResultat(regelModell.getMellomregninger(), andel);
		Optional<FordelAndelModell> matchendeAndel = resultat.getFordelteAndeler().stream()
				.filter(res -> res.getInntektskategori().equals(forventetKategori))
				.findFirst();
		assertThat(matchendeAndel).isPresent();
		assertThat(matchendeAndel.get().erNytt()).isEqualTo(erNytt);
		assertThat(matchendeAndel.get().getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(forventetFordelt));
	}

	private void assertAntallFordelteAndelerResultat(FordelModell regelModell, FordelteAndelerModell andel, int forventetAntallResultatAndeler) {
		FordelteAndelerModell resultat = getResultat(regelModell.getMellomregninger(), andel);
		Assertions.assertThat(resultat.getFordelteAndeler()).hasSize(forventetAntallResultatAndeler);
	}



	private FordelteAndelerModell lagArbeidsandel(Arbeidsforhold ag, Integer brutto, int ønsketBeløpEtterFordeling) {
		FordelAndelModell inputandel = lagFordelAndel(AktivitetStatus.AT, Inntektskategori.ARBEIDSTAKER, ag, brutto);
		FordelteAndelerModell mellomregning = new FordelteAndelerModell(inputandel);
		mellomregning.setMålbeløp(BigDecimal.valueOf(ønsketBeløpEtterFordeling));
		return mellomregning;
	}

	private FordelteAndelerModell lagArbeidsandelUdefinertIK(Arbeidsforhold ag, Integer brutto, int ønsketBeløpEtterFordeling) {
		FordelAndelModell inputandel = lagFordelAndel(AktivitetStatus.AT, Inntektskategori.UDEFINERT, ag, brutto);
		FordelteAndelerModell mellomregning = new FordelteAndelerModell(inputandel);
		mellomregning.setMålbeløp(BigDecimal.valueOf(ønsketBeløpEtterFordeling));
		return mellomregning;
	}

	private FordelteAndelerModell lagSNAndel(Integer brutto, int ønsketBeløpEtterFordeling) {
		FordelAndelModell inputandel = lagFordelAndelUtenArbeidsforhold(AktivitetStatus.SN, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, brutto);
		FordelteAndelerModell mellomregning = new FordelteAndelerModell(inputandel);
		mellomregning.setMålbeløp(BigDecimal.valueOf(ønsketBeløpEtterFordeling));
		return mellomregning;
	}

	private FordelteAndelerModell lagFLAndel(Integer brutto, int ønsketBeløpEtterFordeling) {
		FordelAndelModell inputandel = lagFordelAndel(AktivitetStatus.FL, Inntektskategori.FRILANSER, frilans(), brutto);
		FordelteAndelerModell mellomregning = new FordelteAndelerModell(inputandel);
		mellomregning.setMålbeløp(BigDecimal.valueOf(ønsketBeløpEtterFordeling));
		return mellomregning;
	}

	private FordelteAndelerModell getResultat(List<FordelteAndelerModell> mellomregninger, FordelteAndelerModell andel) {
		Arbeidsforhold forventetAG = andel.getInputAndel().getArbeidsforhold().orElse(null);
		return mellomregninger.stream()
				.filter(mr -> Objects.equals(mr.getInputAndel().getArbeidsforhold().orElse(null), forventetAG))
				.findFirst().orElseThrow();
	}

	private FordelAndelModell lagFordelAndel(AktivitetStatus status, Inntektskategori ik, Arbeidsforhold ag, Integer brutto) {
		FordelAndelModell.Builder fordelAndel = FordelAndelModell.builder()
				.medAktivitetStatus(status)
				.medArbeidsforhold(ag)
				.medInntektskategori(ik);
		return brutto == null ? fordelAndel.build() : fordelAndel.medForeslåttPrÅr(BigDecimal.valueOf(brutto)).build();
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

	private FordelModell kjørRegel(FordelteAndelerModell... regelobjekter) {
		List<FordelteAndelerModell> mellomregninger = Arrays.asList(regelobjekter);
		List<FordelAndelModell> input = mellomregninger.stream().map(FordelteAndelerModell::getInputAndel).toList();
		FordelPeriodeModell periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), LocalDateInterval.TIDENES_ENDE), input);
		FordelModell modell = new FordelModell(periode);
		mellomregninger.forEach(modell::leggTilMellomregningAndel);
		new FordelMålbeløpPrAndel().evaluate(modell);
		return modell;
	}
}