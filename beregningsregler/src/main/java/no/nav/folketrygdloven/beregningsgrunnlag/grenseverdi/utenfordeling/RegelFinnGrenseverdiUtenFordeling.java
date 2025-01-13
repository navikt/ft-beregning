package no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi.utenfordeling;

import no.nav.folketrygdloven.beregningsgrunnlag.fastsette.SjekkBeregningsgrunnlagStørreEnnGrenseverdi;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Fastsatt;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.Specification;

public class RegelFinnGrenseverdiUtenFordeling implements EksportRegel<BeregningsgrunnlagPeriode> {

	public static final String ID = "FP_BR_29_uten_fordeling";

	private BeregningsgrunnlagPeriode regelmodell;

	public RegelFinnGrenseverdiUtenFordeling(BeregningsgrunnlagPeriode regelmodell) {
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

		// Fastsett grenseverdi for fastsetting av beregningsgrunnlag
		Specification<BeregningsgrunnlagPeriode> fastsettGrenseverdi = rs.beregningsRegel(FinnGrenseverdiUtenFordeling.ID, FinnGrenseverdiUtenFordeling.BESKRIVELSE,
				new FinnGrenseverdiUtenFordeling(), new Fastsatt());

		// Fastsett avkortet BG
		Specification<BeregningsgrunnlagPeriode> fastsettAvkortet = rs.beregningsRegel(
				RegelFastsettAndelBGOver6GUtenFordeling.ID,
				RegelFastsettAndelBGOver6GUtenFordeling.BESKRIVELSE,
				new RegelFastsettAndelBGOver6GUtenFordeling(regelmodell).getSpecification(),
				fastsettGrenseverdi);

		// Fastsett uten avkorting
		Specification<BeregningsgrunnlagPeriode> fastsettUtenAvkorting = rs.beregningsRegel(
				"FP_BR_29.6_uten_fordeling",
				"Fastsett BG uten avkorting",
				new FastsettAndelLikBruttoBGUtenFordeling(),
				fastsettGrenseverdi);

		// FP_BR_29.4 4. Brutto beregnings-grunnlag totalt > 6G?
		Specification<BeregningsgrunnlagPeriode> beregnEventuellAvkorting = rs.beregningHvisRegel(
				new SjekkBeregningsgrunnlagStørreEnnGrenseverdi(),
				fastsettAvkortet,
				fastsettUtenAvkorting);

		return beregnEventuellAvkorting;
	}
}
