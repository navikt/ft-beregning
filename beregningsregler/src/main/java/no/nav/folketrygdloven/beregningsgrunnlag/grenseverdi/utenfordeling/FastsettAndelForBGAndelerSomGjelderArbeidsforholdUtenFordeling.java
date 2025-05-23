package no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi.utenfordeling;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettAndelForBGAndelerSomGjelderArbeidsforholdUtenFordeling.ID)
class FastsettAndelForBGAndelerSomGjelderArbeidsforholdUtenFordeling extends LeafSpecification<BeregningsgrunnlagPeriode> {
    static final String ID = "FP_BR 29.8.10_uten_fordeling";
    static final String BESKRIVELSE = "Fastsett brukers andel for alle beregningsgrunnlagsandeler som gjelder arbeidsforhold.";

    FastsettAndelForBGAndelerSomGjelderArbeidsforholdUtenFordeling() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var resultat = ja();
        var atfl = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
        if (atfl != null) {
            Map<String, Object> resultater = new HashMap<>();
            resultat.setEvaluationProperties(resultater);
            atfl.getArbeidsforholdIkkeFrilans().forEach(arbeidsforhold -> {
                var avkortetPrÅr = arbeidsforhold.getInntektsgrunnlagInkludertNaturalytelsePrÅr();
                BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
                    .medAndelsmessigFørGraderingPrAar(avkortetPrÅr)
                    .build();
                resultater.put("avkortetPrÅr" + "." +  arbeidsforhold.getArbeidsgiverId(), avkortetPrÅr);
            });
        }
        return resultat;
    }



}
