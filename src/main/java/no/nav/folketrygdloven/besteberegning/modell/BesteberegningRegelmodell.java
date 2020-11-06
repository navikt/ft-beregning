package no.nav.folketrygdloven.besteberegning.modell;

import no.nav.folketrygdloven.besteberegning.modell.input.BesteberegningInput;
import no.nav.folketrygdloven.besteberegning.modell.output.ForeslåttBesteberegning;

public class BesteberegningRegelmodell {

	private BesteberegningInput input;

	private ForeslåttBesteberegning output = new ForeslåttBesteberegning();

	public BesteberegningRegelmodell(BesteberegningInput input) {
		this.input = input;
	}

	public BesteberegningInput getInput() {
		return input;
	}

	public ForeslåttBesteberegning getOutput() {
		return output;
	}

	public void setOutput(ForeslåttBesteberegning output) {
		this.output = output;
	}
}
