package no.nav.folketrygdloven.besteberegning.modell;

import no.nav.folketrygdloven.besteberegning.modell.input.BesteberegningInput;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegningOutput;

public class BesteberegningRegelmodell {

	private BesteberegningInput input;

	private BesteberegningOutput output = new BesteberegningOutput();

	public BesteberegningRegelmodell(BesteberegningInput input) {
		this.input = input;
	}

	public BesteberegningInput getInput() {
		return input;
	}

	public BesteberegningOutput getOutput() {
		return output;
	}

	public void setOutput(BesteberegningOutput output) {
		this.output = output;
	}
}
