package no.nav.folketrygdloven.besteberegning;

import no.nav.folketrygdloven.besteberegning.modell.BesteberegningRegelmodell;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;


public class RegelForeslåBesteberegning implements EksportRegel<BesteberegningRegelmodell> {

	public static final String ID = "14-7-3";

	@Override
	public Evaluation evaluer(BesteberegningRegelmodell regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Specification<BesteberegningRegelmodell> getSpecification() {
        var rs = new Ruleset<BesteberegningRegelmodell>();

        var fastsettBesteberegning = rs.beregningsRegel(
				FastsettBesteberegningGrunnlag.ID,
				FastsettBesteberegningGrunnlag.BESKRIVELSE,
				new FastsettBesteberegningGrunnlag(),
				new ErSeksBesteMånederBedre());


        var foreslåBesteberegning = rs.beregningsRegel(
				FinnBesteMåneder.ID,
				FinnBesteMåneder.BESKRIVELSE,
				new FinnBesteMåneder(),
				fastsettBesteberegning);

		return foreslåBesteberegning;
	}


}
