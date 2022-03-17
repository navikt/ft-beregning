package no.nav.folketrygdloven.beregningsgrunnlag;

import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningUtfallMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ResultatBeregningType;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.Resultat;
import no.nav.fpsak.nare.evaluation.RuleReasonRef;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSerializer;
import no.nav.fpsak.nare.evaluation.summary.EvaluationSummary;

public class RegelmodellOversetter {

    private RegelmodellOversetter() {
    }

    public static RegelResultat getRegelResultat(Evaluation evaluation, String regelInput) {
        String sporing = getSporing(evaluation);
        EvaluationSummary summary = new EvaluationSummary(evaluation);
        var leafEvaluations = summary.allLeafEvaluations();
		var outcomeCodes = summary.allOutcomes().stream().map(RuleReasonRef::getReasonCode).collect(Collectors.toSet());
		if (outcomeCodes.size() > 1) {
			throw new IllegalStateException("Utviklerfeil "+summary.allOutcomes());
		}
		return leafEvaluations.stream()
				.filter(ev -> ev.getOutcome() != null)
				.findFirst()
				.map(ev -> opprettResultat(ev, regelInput, sporing))
				.orElseGet(() -> opprettResultat(ResultatBeregningType.BEREGNET, regelInput, sporing));

    }

    public static String getSporing(Evaluation evaluation) {
        return EvaluationSerializer.asJson(evaluation);
    }

	private static RegelResultat opprettResultat(Evaluation ev, String regelInput, String sporing) {
		Resultat res = ev.result();
		return switch (res) {
			case JA -> opprettResultat(ResultatBeregningType.BEREGNET, ev, regelInput, sporing);
			case NEI -> opprettResultat(ResultatBeregningType.IKKE_BEREGNET, ev, regelInput, sporing);
			case IKKE_VURDERT -> opprettResultat(ResultatBeregningType.IKKE_VURDERT, regelInput, sporing);
		};
	}

    private static RegelResultat opprettResultat(ResultatBeregningType beregningsresultat, Evaluation ev, String input, String sporing) {
		if (ev.getOutcome() != null) {
			if (ev.getOutcome() instanceof BeregningUtfallMerknad merknad) {
				return new RegelResultat(ResultatBeregningType.IKKE_BEREGNET, input, sporing)
						.medRegelMerknad(new RegelMerknad(merknad.regelUtfallMerknad(), ev.reason()));
			} else {
				throw new IllegalStateException("Utviklerfeil: Ugyldig utfall" + ev.getOutcome());
			}
		} else {
			return new RegelResultat(ResultatBeregningType.BEREGNET, input, sporing);
		}
    }

    private static RegelResultat opprettResultat(ResultatBeregningType beregningsresultat, String input, String sporing) {
        return new RegelResultat(beregningsresultat, input, sporing);
    }

}
