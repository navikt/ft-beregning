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
    private BigDecimal antallGrunnbeløp;

    SjekkBeregningsgrunnlagMindreEnn(BigDecimal antallGrunnbeløp) {
        super(ID, BESKRIVELSE);
        this.antallGrunnbeløp = antallGrunnbeløp;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BigDecimal halvG = grunnlag.getGrunnbeløp().multiply(antallGrunnbeløp);

        SingleEvaluation resultat = (grunnlag.getBruttoPrÅrInkludertNaturalytelser().compareTo(halvG) < 0) ? ja() : nei();
        resultat.setEvaluationProperty("grunnbeløp", grunnlag.getGrunnbeløp());
        resultat.setEvaluationProperty("halvtGrunnbeløp", halvG);
        resultat.setEvaluationProperty("bruttoPrÅr", grunnlag.getBruttoPrÅrInkludertNaturalytelser());
        return resultat;
    }
}
