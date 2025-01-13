package no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi;

import no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi.utenfordeling.RegelFinnGrenseverdiUtenFordeling;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFinnGrenseverdi implements EksportRegel<BeregningsgrunnlagPeriode> {

	public static final String ID = "FP_BR_29";

	private BeregningsgrunnlagPeriode regelmodell;

	public RegelFinnGrenseverdi(BeregningsgrunnlagPeriode regelmodell) {
		this.regelmodell = regelmodell;
	}

	@Override
	public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Specification<BeregningsgrunnlagPeriode> getSpecification() {
		Ruleset<BeregningsgrunnlagPeriode> rs = new Ruleset<>();


		// FP_BR_29.1 Skal finne grenseverdi uten å ta hensyn til fordeling?
		Specification<BeregningsgrunnlagPeriode> skalfinneGrenseverdiUtenFordeling = rs.beregningHvisRegel(
				new SkalFinneGrenseverdiUtenFordeling(),
				new RegelFinnGrenseverdiUtenFordeling(regelmodell).getSpecification(),
				new RegelFinnGrenseverdiMedFordeling(regelmodell).getSpecification());

		return skalfinneGrenseverdiUtenFordeling;
	}
}
