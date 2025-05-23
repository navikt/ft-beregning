package no.nav.folketrygdloven.besteberegning;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.besteberegning.modell.BesteberegningRegelmodell;
import no.nav.folketrygdloven.besteberegning.modell.input.BesteberegningInput;
import no.nav.folketrygdloven.besteberegning.modell.output.AktivitetNøkkel;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegnetAndel;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegnetGrunnlag;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegningOutput;

class ErSeksBesteMånederBedreTest {


	@Test
	void skal_returnere_true_når_beste_6_måneder_gir_høyere_inntekt_en_andel() {
        var output = new BesteberegningOutput();
        var bbGrunnlag = new BesteberegnetGrunnlag(Collections.singletonList(lagAndel(600000, "999")));
		output.setBesteberegnetGrunnlag(bbGrunnlag);
        var bbRegelModell = new BesteberegningRegelmodell(lagInput(500000));
		bbRegelModell.setOutput(output);

		var regel = new ErSeksBesteMånederBedre();

		regel.evaluate(bbRegelModell);

		assertThat(bbRegelModell.getOutput().getSkalBeregnesEtterSeksBesteMåneder()).isTrue();
	}

	@Test
	void skal_returnere_true_når_beste_6_måneder_gir_høyere_inntekt_flere_andeler() {
        var output = new BesteberegningOutput();
        var bbGrunnlag = new BesteberegnetGrunnlag(Arrays.asList(lagAndel(200000, "999"),
				lagAndel(200000, "555"), lagAndel(200000, "777")));
		output.setBesteberegnetGrunnlag(bbGrunnlag);
        var bbRegelModell = new BesteberegningRegelmodell(lagInput(500000));
		bbRegelModell.setOutput(output);

		var regel = new ErSeksBesteMånederBedre();

		regel.evaluate(bbRegelModell);

		assertThat(bbRegelModell.getOutput().getSkalBeregnesEtterSeksBesteMåneder()).isTrue();
	}

	@Test
	void skal_returnere_false_når_beste_6_måneder_gir_lavere_inntekt_flere_andeler() {
        var output = new BesteberegningOutput();
        var bbGrunnlag = new BesteberegnetGrunnlag(Arrays.asList(lagAndel(200000, "999"),
				lagAndel(200000, "555"), lagAndel(199999, "777")));
		output.setBesteberegnetGrunnlag(bbGrunnlag);
        var bbRegelModell = new BesteberegningRegelmodell(lagInput(600000));
		bbRegelModell.setOutput(output);

		var regel = new ErSeksBesteMånederBedre();

		regel.evaluate(bbRegelModell);

		assertThat(bbRegelModell.getOutput().getSkalBeregnesEtterSeksBesteMåneder()).isFalse();
	}

	@Test
	void skal_returnere_false_når_beste_6_måneder_gir_samme_inntekt_flere_andeler() {
        var output = new BesteberegningOutput();
        var bbGrunnlag = new BesteberegnetGrunnlag(Arrays.asList(lagAndel(200000, "999"),
				lagAndel(200000, "555"), lagAndel(200000, "777")));
		output.setBesteberegnetGrunnlag(bbGrunnlag);
        var bbRegelModell = new BesteberegningRegelmodell(lagInput(600000));
		bbRegelModell.setOutput(output);

		var regel = new ErSeksBesteMånederBedre();

		regel.evaluate(bbRegelModell);

		assertThat(bbRegelModell.getOutput().getSkalBeregnesEtterSeksBesteMåneder()).isFalse();
	}

	private BesteberegningInput lagInput(int beregnet) {
		return BesteberegningInput.builder()
				.medSkjæringstidspunktOpptjening(LocalDate.now())
				.medBeregnetGrunnlag(BigDecimal.valueOf(beregnet))
				.build();
	}

	private BesteberegnetAndel lagAndel(int inntekt, String orgnr) {
		return new BesteberegnetAndel(lagAktivitetNøkkel(orgnr, "test"), BigDecimal.valueOf(inntekt));
	}

	public AktivitetNøkkel lagAktivitetNøkkel(String orgnr, String arbforId) {
		return AktivitetNøkkel.forOrganisasjon(orgnr, arbforId);
	}
}
