package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FordelModell {
	private final FordelPeriodeModell input;
	private List<FordelAndelModell> fordelteAndeler = new ArrayList<>();

	public FordelModell(FordelPeriodeModell input) {
		Objects.requireNonNull(input, "input");
		this.input = input;
	}

	public void leggTilFordeltAndel(FordelAndelModell andel) {
		Objects.requireNonNull(andel,"andel");
		this.fordelteAndeler.add(andel);
	}

	public FordelPeriodeModell getInput() {
		return input;
	}
}
