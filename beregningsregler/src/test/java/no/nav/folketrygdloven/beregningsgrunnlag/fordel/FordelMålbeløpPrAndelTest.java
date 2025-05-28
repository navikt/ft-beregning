package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelteAndelerModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.fpsak.tidsserie.LocalDateInterval;

class FordelMålbeløpPrAndelTest {

	@Test
	void skal_teste_fordeling_mellom_arbeidsforhold_med_refusjon_med_samme_inntektskategori() {
		// Arrange
        var andel1 = lagArbeidsandel(arbeid("999", "abc"), 200_000, 150_000);
        var andel2 = lagArbeidsandel(arbeid("888", "abc"), null, 50_000);

		// Act
        var regelModell = kjørRegel(andel1, andel2);

		// Assert
		assertThat(regelModell.getMellomregninger()).hasSize(2);

		assertAntallFordelteAndelerResultat(regelModell, andel1, 1);
		assertFordeltAndel(regelModell, andel1, 150000, Inntektskategori.ARBEIDSTAKER);

		assertAntallFordelteAndelerResultat(regelModell, andel2, 1);
		assertFordeltAndel(regelModell, andel2, 50000, Inntektskategori.ARBEIDSTAKER);
	}

	@Test
	void skal_teste_fordeling_fra_en_eksisterende_med_ref_til_to_tilkomne() {
		// Arrange
        var andel1 = lagArbeidsandel(arbeid("999", "abc"), 200_000, 150_000);
        var andel2 = lagArbeidsandel(arbeid("888", "abc"), null, 20_000);
        var andel3 = lagArbeidsandel(arbeid("777", "abc"), null, 30_000);

		// Act
        var regelModell = kjørRegel(andel1, andel2, andel3);

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
	void skal_overskrive_andel_med_inntektskategori_udefinert() {
		// Arrange
        var andel1 = lagArbeidsandel(arbeid("999", "abc"), 400_000, 150_000);
        var andel2 = lagArbeidsandelUdefinertIK(arbeid("888", "abc"), null, 250_000);

		// Act
        var regelModell = kjørRegel(andel1, andel2);

		// Assert
		assertThat(regelModell.getMellomregninger()).hasSize(2);

		assertAntallFordelteAndelerResultat(regelModell, andel1, 1);
		assertFordeltAndel(regelModell, andel1, 150000, Inntektskategori.ARBEIDSTAKER, false);

		assertAntallFordelteAndelerResultat(regelModell, andel2, 1);
		assertFordeltAndel(regelModell, andel2, 250000, Inntektskategori.ARBEIDSTAKER, false);
	}

	@Test
	void skal_teste_fordeling_fra_næring_og_arbeid_til_to_tilkomne_likt_fordelt() {
		// Arrange
        var atAndel = lagArbeidsandel(arbeid("999", "abc"), 60_000, 60_000);
        var snAndel = lagSNAndel(100_000, 0);
        var tilkommetAT1 = lagArbeidsandel(arbeid("888", "abc"), null, 45_000);
        var tilkommetAT2 = lagArbeidsandel(arbeid("777", "abc"), null, 55_000);

		// Act
        var regelModell = kjørRegel(atAndel, snAndel, tilkommetAT1, tilkommetAT2);

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
	void skal_fordele_fra_tre_eksisterende_til_et_tilkommet_arbeidsforhold() {
		// Arrange
        var andel1 = lagArbeidsandel(arbeid("999", "abc"), 200_000, 50_000);
        var andel2 = lagArbeidsandel(arbeid("888", "abc"), 100_000, 100_000);
        var andel3 = lagArbeidsandel(arbeid("777", "abc"), 50_000, 0);
        var andel4 = lagArbeidsandel(arbeid("666", "abc"), null, 200_000);

		// Act
        var regelModell = kjørRegel(andel1, andel2, andel3, andel4);

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
	void skal_fordele_fra_frilans_og_næring_og_arbeid_til_tilkommet_arbeid() {
		// Arrange
        var arbeid = lagArbeidsandel(arbeid("999", "abc"), 200_000, 150_000);
        var frilans = lagFLAndel(100_000, 0);
        var næring = lagSNAndel(300_000, 0);
        var tilkommetArbeid = lagArbeidsandel(arbeid("888", "abc"), null, 450_000);

		// Act
        var regelModell = kjørRegel(arbeid, frilans, næring, tilkommetArbeid);

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
        var resultat = getResultat(regelModell.getMellomregninger(), andel);
        var matchendeAndel = resultat.getFordelteAndeler().stream()
				.filter(res -> res.getInntektskategori().equals(forventetKategori))
				.findFirst();
		assertThat(matchendeAndel).isPresent();
		assertThat(matchendeAndel.get().getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(forventetFordelt));
	}

	private void assertFordeltAndel(FordelModell regelModell, FordelteAndelerModell andel, int forventetFordelt, Inntektskategori forventetKategori, boolean erNytt) {
        var resultat = getResultat(regelModell.getMellomregninger(), andel);
        var matchendeAndel = resultat.getFordelteAndeler().stream()
				.filter(res -> res.getInntektskategori().equals(forventetKategori))
				.findFirst();
		assertThat(matchendeAndel).isPresent();
		assertThat(matchendeAndel.get().erNytt()).isEqualTo(erNytt);
		assertThat(matchendeAndel.get().getFordeltPrÅr().orElseThrow()).isEqualByComparingTo(BigDecimal.valueOf(forventetFordelt));
	}

	private void assertAntallFordelteAndelerResultat(FordelModell regelModell, FordelteAndelerModell andel, int forventetAntallResultatAndeler) {
        var resultat = getResultat(regelModell.getMellomregninger(), andel);
		Assertions.assertThat(resultat.getFordelteAndeler()).hasSize(forventetAntallResultatAndeler);
	}



	private FordelteAndelerModell lagArbeidsandel(Arbeidsforhold ag, Integer brutto, int ønsketBeløpEtterFordeling) {
        var inputandel = lagFordelAndel(AktivitetStatus.AT, Inntektskategori.ARBEIDSTAKER, ag, brutto);
        var mellomregning = new FordelteAndelerModell(inputandel);
		mellomregning.setMålbeløp(BigDecimal.valueOf(ønsketBeløpEtterFordeling));
		return mellomregning;
	}

	private FordelteAndelerModell lagArbeidsandelUdefinertIK(Arbeidsforhold ag, Integer brutto, int ønsketBeløpEtterFordeling) {
        var inputandel = lagFordelAndel(AktivitetStatus.AT, Inntektskategori.UDEFINERT, ag, brutto);
        var mellomregning = new FordelteAndelerModell(inputandel);
		mellomregning.setMålbeløp(BigDecimal.valueOf(ønsketBeløpEtterFordeling));
		return mellomregning;
	}

	private FordelteAndelerModell lagSNAndel(Integer brutto, int ønsketBeløpEtterFordeling) {
        var inputandel = lagFordelAndelUtenArbeidsforhold(AktivitetStatus.SN, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, brutto);
        var mellomregning = new FordelteAndelerModell(inputandel);
		mellomregning.setMålbeløp(BigDecimal.valueOf(ønsketBeløpEtterFordeling));
		return mellomregning;
	}

	private FordelteAndelerModell lagFLAndel(Integer brutto, int ønsketBeløpEtterFordeling) {
        var inputandel = lagFordelAndel(AktivitetStatus.FL, Inntektskategori.FRILANSER, frilans(), brutto);
        var mellomregning = new FordelteAndelerModell(inputandel);
		mellomregning.setMålbeløp(BigDecimal.valueOf(ønsketBeløpEtterFordeling));
		return mellomregning;
	}

	private FordelteAndelerModell getResultat(List<FordelteAndelerModell> mellomregninger, FordelteAndelerModell andel) {
        var forventetAG = andel.getInputAndel().getArbeidsforhold().orElse(null);
		return mellomregninger.stream()
				.filter(mr -> Objects.equals(mr.getInputAndel().getArbeidsforhold().orElse(null), forventetAG))
				.findFirst().orElseThrow();
	}

	private FordelAndelModell lagFordelAndel(AktivitetStatus status, Inntektskategori ik, Arbeidsforhold ag, Integer brutto) {
        var fordelAndel = FordelAndelModell.builder()
				.medAktivitetStatus(status)
				.medArbeidsforhold(ag)
				.medInntektskategori(ik);
		return brutto == null ? fordelAndel.build() : fordelAndel.medForeslåttPrÅr(BigDecimal.valueOf(brutto)).build();
	}

	private FordelAndelModell lagFordelAndelUtenArbeidsforhold(AktivitetStatus status, Inntektskategori ik, Integer brutto) {
        var fordelAndel = FordelAndelModell.builder()
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
        var mellomregninger = Arrays.asList(regelobjekter);
        var input = mellomregninger.stream().map(FordelteAndelerModell::getInputAndel).collect(Collectors.toList());
        var periode = new FordelPeriodeModell(Periode.of(LocalDate.now(), LocalDateInterval.TIDENES_ENDE), input);
        var modell = new FordelModell(periode);
		mellomregninger.forEach(modell::leggTilMellomregningAndel);
		new FordelMålbeløpPrAndel().evaluate(modell);
		return modell;
	}
}
