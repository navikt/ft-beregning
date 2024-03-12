package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.AvslagUnderEnG;
import no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker.AvslagUnderEnHalvG;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Innvilget;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.regelmodelloversetter.EksportRegel;
import no.nav.fpsak.nare.Ruleset;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.ConditionalOrSpecification;
import no.nav.fpsak.nare.specification.Specification;

public class RegelVurderBeregningsgrunnlag implements EksportRegel<BeregningsgrunnlagPeriode> {

	public static final String ID = "FP_BR_29";

	private static final Ruleset<BeregningsgrunnlagPeriode> RS = new Ruleset<>();

	@Override
	public Evaluation evaluer(BeregningsgrunnlagPeriode regelmodell) {
		return getSpecification().evaluate(regelmodell);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Specification<BeregningsgrunnlagPeriode> getSpecification() {
		/*
		hjemler for sjekk mot minstekrav på 0.5 G for beregningsgrunnlag:
			Foreldrepenger: Folketrygdloven §14-7 1. ledd
			Svangerskapspenger: Folketrygdloven §14-4 3. ledd
			Pleiepenger,omsorgspenger: Folketrygdloven §9-3 2. ledd
		for midlertidig inaktiv (vurderes kun for k9-ytelsene) Folketrygdloven §8-47 5.ledd
		 */

		Specification<BeregningsgrunnlagPeriode> sjekkMotHalvG = RS.beregningHvisRegel(new BeregningsgrunnlagUnderHalvG(), new AvslagUnderEnHalvG(), new Innvilget());
		Specification<BeregningsgrunnlagPeriode> sjekkMotEnG = RS.beregningHvisRegel(new BeregningsgrunnlagUnderEnG(), new AvslagUnderEnG(), new Innvilget());
		Specification<BeregningsgrunnlagPeriode> k9Regel = RS.beregningHvisRegel(new ErMidlertidigInaktiv(), sjekkMotEnG, sjekkMotHalvG);
		Specification<BeregningsgrunnlagPeriode> ompRegel = RS.beregningHvisRegel(new SjekkErOmsorgspengerTilArbeidsgiver(), new Innvilget(), k9Regel);

		return new ConditionalOrSpecification.Builder<BeregningsgrunnlagPeriode>(ID, "Velg regel for ytelse")
				.hvis(new GjelderForeldrepenger(), sjekkMotHalvG)
				.hvis(new GjelderSvangerskapspenger(), sjekkMotHalvG)
				.hvis(new GjelderOmsorgspenger(), ompRegel)
				.ellers(k9Regel);
	}
}
