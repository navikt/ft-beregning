package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FordelModell {
	private final FordelPeriodeModell input;
	private List<FordelAndelModellMellomregning> mellomregninger = new ArrayList<>();

	public FordelModell(FordelPeriodeModell input) {
		Objects.requireNonNull(input, "input");
		this.input = input;
	}

	public FordelPeriodeModell getInput() {
		return input;
	}

	public void leggTilMellomregningAndel(FordelAndelModellMellomregning mellomregning) {
		this.mellomregninger.add(mellomregning);
	}

	public List<FordelAndelModellMellomregning> getMellomregninger() {
		return mellomregninger;
	}
}
