package no.nav.foreldrepenger.beregningsgrunnlag;

import java.util.Collection;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSummary;

public class RegelmodellOversetter {

    private RegelmodellOversetter() {
    }

    public static RegelResultat getRegelResultat(Evaluation evaluation, String regelInput) {
        String sporing = getSporing(evaluation);
        EvaluationSummary summary = new EvaluationSummary(evaluation);
        Collection<Evaluation> leafEvaluations = summary.leafEvaluations();
        for (Evaluation ev : leafEvaluations) {
            if (ev.getOutcome() != null) {
                Resultat res = ev.result();
                switch (res) {
                    case JA:
                        return opprettResultat(ResultatBeregningType.BEREGNET, regelInput, sporing);
                    case NEI:
                        return opprettResultat(ResultatBeregningType.IKKE_BEREGNET, ev, regelInput, sporing);
                    case IKKE_VURDERT:
                        return opprettResultat(ResultatBeregningType.IKKE_VURDERT, regelInput, sporing);
                    default:
                        throw new IllegalArgumentException("Ukjent Resultat:" + res + " ved evaluering av:" + ev);
                }
            }
        }
        return opprettResultat(ResultatBeregningType.BEREGNET, regelInput, sporing);
    }

    public static String getSporing(Evaluation evaluation) {
        return EvaluationSerializer.asJson(evaluation);
    }

    private static RegelResultat opprettResultat(ResultatBeregningType beregningsresultat, Evaluation ev, String input, String sporing) {
        return new RegelResultat(beregningsresultat, input, sporing).medRegelMerknad(new RegelMerknad(ev.getOutcome().getReasonCode(), ev.reason()));
    }

    private static RegelResultat opprettResultat(ResultatBeregningType beregningsresultat, String input, String sporing) {
        return new RegelResultat(beregningsresultat, input, sporing);
    }

}
