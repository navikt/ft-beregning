package no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi.utenfordeling;


import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmTotaltBGForArbeidsforholdStørreEnnGrenseverdiUtenFordeling.ID)
class SjekkOmTotaltBGForArbeidsforholdStørreEnnGrenseverdiUtenFordeling extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR_29.8.2_uten_fordeling";
    static final String BESKRIVELSE = "Er totalt intektsgrunnlag for andeler fra arbeidsforhold større enn 6G?";

    SjekkOmTotaltBGForArbeidsforholdStørreEnnGrenseverdiUtenFordeling() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var grenseverdi = grunnlag.getGrenseverdi();
        var atfl = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        var totaltBG = atfl == null ? BigDecimal.ZERO : atfl.getArbeidsforholdIkkeFrilans().stream()
            .map(BeregningsgrunnlagPrArbeidsforhold::getInntektsgrunnlagInkludertNaturalytelsePrÅr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        var resultat = totaltBG.compareTo(grenseverdi) > 0 ? ja() : nei();
        resultat.setEvaluationProperty("totaltBeregningsgrunnlagFraArbeidsforhold", totaltBG);
        resultat.setEvaluationProperty("grunnbeløp", grunnlag.getGrunnbeløp());
        resultat.setEvaluationProperty("grenseverdi", grenseverdi);
        return resultat;
    }
}
