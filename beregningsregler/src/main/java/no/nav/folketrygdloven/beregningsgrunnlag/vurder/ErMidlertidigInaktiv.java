package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ErMidlertidigInaktiv.ID)
class ErMidlertidigInaktiv extends LeafSpecification<BeregningsgrunnlagPeriode> {

	static final String ID = "FP_VK_8_47";

	ErMidlertidigInaktiv() {
		super(ID, "Var søker midlertidig ute av inntektsgivende arbeid på skjæringstidspunktet?");
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var midlertidigInaktiv = grunnlag.getBeregningsgrunnlag().getMidlertidigInaktivType() != null;
		return midlertidigInaktiv ? ja() : nei();
	}


}
