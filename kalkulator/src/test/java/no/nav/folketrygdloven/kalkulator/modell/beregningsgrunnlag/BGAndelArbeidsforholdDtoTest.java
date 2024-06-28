package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;

class BGAndelArbeidsforholdDtoTest {

	private static final Arbeidsgiver AG = Arbeidsgiver.virksomhet("999999999");

	@Test
	void saksbehandlet_refusjon_skal_sette_refusjon() {
		var arb = BGAndelArbeidsforholdDto.builder()
				.medArbeidsgiver(AG)
				.medSaksbehandletRefusjonPrÅr(Beløp.fra(100))
				.build();

		assertThat(arb.getRefusjon().isPresent()).isTrue();
		assertThat(arb.getRefusjon().get().getSaksbehandletRefusjonPrÅr().compareTo(Beløp.fra(100))).isEqualTo(0);
	}


	@Test
	void saksbehandlet_refusjon_skal_ignorere_null_om_ikke_satt_fra_før() {
		var arb = BGAndelArbeidsforholdDto.builder()
				.medArbeidsgiver(AG)
				.medSaksbehandletRefusjonPrÅr(null)
				.build();

		assertThat(arb.getRefusjon().isPresent()).isFalse();
	}

	@Test
	void saksbehandlet_refusjon_skal_nulle_ut_refusjon_om_satt() {
		var arb = BGAndelArbeidsforholdDto.builder()
				.medArbeidsgiver(AG)
				.medSaksbehandletRefusjonPrÅr(Beløp.fra(100))
				.build();

		var ny = BGAndelArbeidsforholdDto.builder(arb).medSaksbehandletRefusjonPrÅr(null).build();

		assertThat(ny.getRefusjon().isPresent()).isFalse();
	}

	@Test
	void saksbehandlet_refusjon_skal_kun_nulle_ut_felt_om_andre_felter_satt() {
		var arb = BGAndelArbeidsforholdDto.builder()
				.medArbeidsgiver(AG)
				.medRefusjonskravPrÅr(Beløp.ZERO, Utfall.UNDERKJENT)
				.medSaksbehandletRefusjonPrÅr(Beløp.fra(100))
				.build();

		var ny = BGAndelArbeidsforholdDto.builder(arb).medSaksbehandletRefusjonPrÅr(null).build();

		assertThat(ny.getRefusjon().isPresent()).isTrue();
		assertThat(ny.getRefusjon().get().getSaksbehandletRefusjonPrÅr()).isNull();
	}

}