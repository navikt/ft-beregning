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
		//splittet i ulike regler pga ulik hjemmel
		Specification<BeregningsgrunnlagPeriode> sjekkMotHalvGFP = RS.beregningHvisRegel(new HarForLiteBeregningsgrunnlagFP(), new AvslagUnderEnHalvG(), new Innvilget());
		Specification<BeregningsgrunnlagPeriode> sjekkMotHalvGSVP = RS.beregningHvisRegel(new HarForLiteBeregningsgrunnlagSVP(), new AvslagUnderEnHalvG(), new Innvilget());
		Specification<BeregningsgrunnlagPeriode> sjekkMotHalvGK9 = RS.beregningHvisRegel(new HarForLiteBeregningsgrunnlagKap9(), new AvslagUnderEnHalvG(), new Innvilget());

		Specification<BeregningsgrunnlagPeriode> sjekkMidlertidigAleneMot1G = RS.beregningHvisRegel(new HarForLiteBeregningsgrunnlagMidlertidigInaktiv(), new AvslagUnderEnG(), new Innvilget());
		Specification<BeregningsgrunnlagPeriode> k9Regel = RS.beregningHvisRegel(new ErMidlertidigInaktiv(), sjekkMidlertidigAleneMot1G, sjekkMotHalvGK9);

		Specification<BeregningsgrunnlagPeriode> ompRegel = RS.beregningHvisRegel(new SjekkErOmsorgspengerTilArbeidsgiver(), new Innvilget(), k9Regel);

		return new ConditionalOrSpecification.Builder<BeregningsgrunnlagPeriode>(ID, "Velg regel for ytelse")
				.hvis(new GjelderForeldrepenger(), sjekkMotHalvGFP)
				.hvis(new GjelderSvangerskapspenger(), sjekkMotHalvGSVP)
				.hvis(new GjelderOmsorgspenger(), ompRegel)
				.ellers(k9Regel);
	}
}
