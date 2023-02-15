package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.fp.ForeldrepengerGrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(GjelderForeldrepenger.ID)
class GjelderForeldrepenger extends LeafSpecification<BeregningsgrunnlagPeriode> {

	static final String ID = "FP_BR_29";

	GjelderForeldrepenger() {
		super(ID, "Gjelder det foreldrepenger?");
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		return grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag() instanceof ForeldrepengerGrunnlag ? ja() : nei();
	}


}
