package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkBeregningsgrunnlagMindreEnn.ID)
class SjekkBeregningsgrunnlagMindreEnn extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_VK_32.1";
    static final String BESKRIVELSE = "Er beregningsgrunnlag mindre enn en 0,5G?";

    SjekkBeregningsgrunnlagMindreEnn() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BigDecimal minstekrav = grunnlag.getGrunnbeløpForVilkårsvurdering().multiply(grunnlag.getAntallGMinstekravVilkår());

        SingleEvaluation resultat = (grunnlag.getBruttoPrÅr().compareTo(minstekrav) < 0) ? ja() : nei();
        resultat.setEvaluationProperty("grunnbeløpForVilkårsvurdering", grunnlag.getGrunnbeløpForVilkårsvurdering());
        resultat.setEvaluationProperty("halvtGrunnbeløp", minstekrav);
        resultat.setEvaluationProperty("faktiskGrunnbeløp", grunnlag.getGrunnbeløp());
        resultat.setEvaluationProperty("bruttoPrÅr", grunnlag.getBruttoPrÅr());
        return resultat;
    }
}
