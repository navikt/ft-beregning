package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class SjekkOmRefusjonOverstigerBeregningsgrunnlag extends LeafSpecification<BeregningsgrunnlagPeriode> {

    private static final String ID = "FP_BR 22.3.7";
    private static final String BESKRIVELSE = "Sjekk om refusjon overstiger beregningsgrunnlag for arbeidsforhold.";
    private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

    SjekkOmRefusjonOverstigerBeregningsgrunnlag(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
        super(ID, BESKRIVELSE);
        this.arbeidsforhold = arbeidsforhold;
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        BigDecimal refusjonskravPrÅr = arbeidsforhold.getRefusjonskravPrÅr().orElse(BigDecimal.ZERO);
        BigDecimal bruttoInkludertNaturalytelsePrÅr = arbeidsforhold.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO);
        BigDecimal refusjonBruttoBgDiff = refusjonskravPrÅr.subtract(bruttoInkludertNaturalytelsePrÅr);
        SingleEvaluation resultat = refusjonBruttoBgDiff.compareTo(BigDecimal.ZERO) > 0 ? ja() : nei();
        resultat.setEvaluationProperty("refusjonskravPrÅr." + arbeidsforhold.getArbeidsgiverId(), refusjonskravPrÅr);
        resultat.setEvaluationProperty("bruttoInklNaturalytelsePrÅr." + arbeidsforhold.getArbeidsgiverId(), bruttoInkludertNaturalytelsePrÅr);
        return resultat;
    }
}
