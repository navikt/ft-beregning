package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett;

import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class Fastsatt extends LeafSpecification<BeregningsgrunnlagPeriode> {

	public Fastsatt() {
		super("Fastsatt");
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		return ja();
	}

	@Override
	public String beskrivelse() {
		return "Fastsatt";
	}
}
