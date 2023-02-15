package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp.OmsorgspengerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(GjelderOmsorgspenger.ID)
class GjelderOmsorgspenger extends LeafSpecification<BeregningsgrunnlagPeriode> {

	static final String ID = "FP_VK_x";

	GjelderOmsorgspenger() {
		super(ID, "Gjelder det omsorgspenger?");
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		return grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag() instanceof OmsorgspengerGrunnlag ? ja() : nei();
	}


}
