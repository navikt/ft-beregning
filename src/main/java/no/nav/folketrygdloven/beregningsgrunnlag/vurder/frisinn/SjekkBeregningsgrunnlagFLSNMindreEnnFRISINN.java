package no.nav.folketrygdloven.beregningsgrunnlag.vurder.frisinn;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

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

        BeregningsgrunnlagPrStatus atflStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        BigDecimal frilansInntekt = atflStatus == null
            ? BigDecimal.ZERO
            : atflStatus.getFrilansArbeidsforhold()
            .flatMap(BeregningsgrunnlagPrArbeidsforhold::getBruttoInkludertNaturalytelsePrÅr)
            .orElse(BigDecimal.ZERO);
        BeregningsgrunnlagPrStatus snStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
        BigDecimal snInntekt = snStatus == null
            ? BigDecimal.ZERO
            : snStatus.getBruttoInkludertNaturalytelsePrÅr();
        BigDecimal sumFLSN = snInntekt.add(frilansInntekt);
        SingleEvaluation resultat = sumFLSN.compareTo(minstekrav) < 0 ? ja() : nei();
        resultat.setEvaluationProperty("grunnbeløp", grunnlag.getGrunnbeløp());
        resultat.setEvaluationProperty("treKvartGrunnbeløp", minstekrav);
        resultat.setEvaluationProperty("bruttoPrÅrSNFL", sumFLSN);
        return resultat;
    }
}
