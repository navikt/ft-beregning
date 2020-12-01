package no.nav.folketrygdloven.besteberegning;

import no.nav.folketrygdloven.besteberegning.modell.BesteberegningRegelmodell;
import no.nav.fpsak.nare.RuleService;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


public class RegelForeslåBesteberegning implements RuleService<BesteberegningRegelmodell> {

	public static final String ID = "14-7-3";

	@Override
	public Evaluation evaluer(BesteberegningRegelmodell regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Specification<BesteberegningRegelmodell> getSpecification() {
		Ruleset<BesteberegningRegelmodell> rs = new Ruleset<>();

		Specification<BesteberegningRegelmodell> foreslåBesteberegning = rs.beregningsRegel(
				FinnBesteMåneder.ID,
				FinnBesteMåneder.BESKRIVELSE,
				new FinnBesteMåneder(),
				new FastsettBesteberegningGrunnlag());

		return foreslåBesteberegning;
	}



}
