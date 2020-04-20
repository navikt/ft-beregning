package no.nav.folketrygdloven.beregningsgrunnlag.vurder.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.math.BigDecimal;

@RuleDocumentation(SjekkBeregningsgrunnlagFLSNMindreEnnFRISINN.ID)
class SjekkBeregningsgrunnlagFLSNMindreEnnFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FRISINN 3.2";
    static final String BESKRIVELSE = "Er beregningsgrunnlag fra SN/FL mindre enn en 0,75G?";

    SjekkBeregningsgrunnlagFLSNMindreEnnFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BigDecimal minstekrav = grunnlag.getGrunnbeløp().multiply(grunnlag.getAntallGMinstekravVilkår());
        BigDecimal bruttoForSøkteAndeler = BigDecimal.ZERO;

        BeregningsgrunnlagPrStatus atflStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        BeregningsgrunnlagPrArbeidsforhold frilansandel = atflStatus == null ? null : atflStatus.getFrilansArbeidsforhold().orElse(null);
        if (frilansandel != null && frilansandel.getUtbetalingsprosentSVP() != null) {
            if (frilansandel.getUtbetalingsprosentSVP().compareTo(BigDecimal.ZERO) > 0) {
                bruttoForSøkteAndeler = bruttoForSøkteAndeler.add(frilansandel.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO));
           }
        }
        BeregningsgrunnlagPrStatus snStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        if (snStatus != null && snStatus.getUtbetalingsprosent() != null) {
            if (snStatus.getUtbetalingsprosent().compareTo(BigDecimal.ZERO) > 0) {
                bruttoForSøkteAndeler = bruttoForSøkteAndeler.add(snStatus.getBruttoInkludertNaturalytelsePrÅr());
            }
        }

        SingleEvaluation resultat = bruttoForSøkteAndeler.compareTo(minstekrav) < 0 ? ja() : nei();
        resultat.setEvaluationProperty("grunnbeløp", grunnlag.getGrunnbeløp());
        resultat.setEvaluationProperty("treKvartGrunnbeløp", minstekrav);
        resultat.setEvaluationProperty("bruttoPrÅrSNFL", bruttoForSøkteAndeler);
        return resultat;
    }
}
