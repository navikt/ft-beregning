package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import static no.nav.folketrygdloven.beregningsgrunnlag.vurder.FinnGrunnbeløpForVilkårsvurdering.finnGrunnbeløpForVilkårsvurdering;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class SjekkBeregningsgrunnlagMindreEnnAntallG extends LeafSpecification<BeregningsgrunnlagPeriode> {
	private BigDecimal minimumAntallG;

	SjekkBeregningsgrunnlagMindreEnnAntallG(String id, BigDecimal minimumAntallG) {
		super(id, "Er beregningsgrunnlag mindre enn " + minimumAntallG.toPlainString() + "?");
		this.minimumAntallG = minimumAntallG;
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		BigDecimal grunnbeløpForVilkårsvurdering = finnGrunnbeløpForVilkårsvurdering(grunnlag);

		BigDecimal minstekrav = grunnbeløpForVilkårsvurdering.multiply(minimumAntallG);

		SingleEvaluation resultat = (grunnlag.getBruttoPrÅr().compareTo(minstekrav) < 0) ? ja() : nei();
		resultat.setEvaluationProperty("grunnbeløpForVilkårsvurdering", grunnbeløpForVilkårsvurdering);
		resultat.setEvaluationProperty("minstegravAntallG", minimumAntallG);
		resultat.setEvaluationProperty("minstekravBeløp", minstekrav);
		resultat.setEvaluationProperty("faktiskGrunnbeløp", grunnlag.getGrunnbeløp());
		resultat.setEvaluationProperty("bruttoPrÅr", grunnlag.getBruttoPrÅr());
		return resultat;
	}


}
