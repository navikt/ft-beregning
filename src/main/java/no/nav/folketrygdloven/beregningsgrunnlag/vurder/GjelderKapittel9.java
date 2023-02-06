package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(GjelderKapittel9.ID)
class GjelderKapittel9 extends LeafSpecification<BeregningsgrunnlagPeriode> {

	static final String ID = "FP_VK_x";

	GjelderKapittel9() {
		super(ID, "Gjelder det en kap.9-ytelse (pleiepenger, omsorgspenger, opplæringspenger)?");
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		return grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag().erKap9Ytelse() ? ja() : nei();
	}


}
