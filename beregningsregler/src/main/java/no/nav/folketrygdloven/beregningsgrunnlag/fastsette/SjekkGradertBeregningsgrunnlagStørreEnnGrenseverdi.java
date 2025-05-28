package no.nav.folketrygdloven.beregningsgrunnlag.fastsette;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkGradertBeregningsgrunnlagStørreEnnGrenseverdi.ID)
public class SjekkGradertBeregningsgrunnlagStørreEnnGrenseverdi extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FP_BR_29.4";
    public static final String BESKRIVELSE = "Er beregningsgrunnlag større enn 6G/grenseverdi";

    public SjekkGradertBeregningsgrunnlagStørreEnnGrenseverdi() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

        var bruttoInklBortfaltNaturalytelsePrÅr = grunnlag.getAktivitetsgradertBruttoPrÅrInkludertNaturalytelser();
        var grenseverdi = grunnlag.getGrenseverdi();
        var resultat = bruttoInklBortfaltNaturalytelsePrÅr.compareTo(grenseverdi) > 0 ? ja() : nei();
        resultat.setEvaluationProperty("bruttoPrÅr", bruttoInklBortfaltNaturalytelsePrÅr);
        resultat.setEvaluationProperty("grunnbeløp", grunnlag.getGrunnbeløp());
        resultat.setEvaluationProperty("grenseverdi", grenseverdi);
        return resultat;
    }
}
