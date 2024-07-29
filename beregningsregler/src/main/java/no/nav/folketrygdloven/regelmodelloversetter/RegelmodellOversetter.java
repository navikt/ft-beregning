package no.nav.folketrygdloven.regelmodelloversetter;

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
import no.nav.fpsak.nare.evaluation.summary.NareVersion;

public class RegelmodellOversetter {


	private RegelmodellOversetter() {
	}

	public static RegelResultat getRegelResultat(Evaluation evaluation, String regelInput) {
		String sporing = getSporing(evaluation);
		EvaluationSummary summary = new EvaluationSummary(evaluation);
		var leafEvaluations = summary.allLeafEvaluations();
		var outcomeCodes = summary.allOutcomes().stream().map(RuleReasonRef::getReasonCode).collect(Collectors.toSet());
		if (outcomeCodes.size() > 1) {
			throw new IllegalStateException("Utviklerfeil " + summary.allOutcomes());
		}
		return leafEvaluations.stream()
				.filter(ev -> ev.getOutcome() != null)
				.findFirst()
				.map(ev -> opprettResultat(ev, regelInput, sporing))
				.orElseGet(() -> opprettResultat(ResultatBeregningType.BEREGNET, regelInput, sporing));

	}

	public static String getSporing(Evaluation evaluation) {
		return EvaluationSerializer.asJson(evaluation, NareVersion.NARE_VERSION, BeregningRegelmodellVersjon.BEREGNING_REGELMODELL_VERSJON);
	}

	public static String getRegelVersjonsNummer() {
		return BeregningRegelmodellVersjon.BEREGNING_REGELMODELL_VERSJON.nameAndVersion();
	}

	private static RegelResultat opprettResultat(Evaluation ev, String regelInput, String sporing) {
		Resultat res = ev.result();
		return switch (res) {
			case JA, NEI -> opprettResultat(res, ev, regelInput, sporing);
			case IKKE_VURDERT -> opprettResultat(ResultatBeregningType.IKKE_VURDERT, regelInput, sporing);
		};
	}

	private static RegelResultat opprettResultat(Resultat resultat, Evaluation ev, String input, String sporing) {
		if (ev.getOutcome() == null && resultat != Resultat.JA) {
			throw new IllegalStateException("Inkonsistent resultat. Har " + resultat + " og samtidig er Evaluation.getOutcome null");
		}
		if (ev.getOutcome() instanceof BeregningUtfallMerknad && resultat != Resultat.NEI) {
			throw new IllegalArgumentException("Inkonsistet resultat. Har " + resultat + " og samtidig er Evaluation.getOutcome en BeregningUtfallMerknad");
		}

		if (ev.getOutcome() == null) {
			return new RegelResultat(ResultatBeregningType.BEREGNET, getRegelVersjonsNummer(), input, sporing);
		} else if (ev.getOutcome() instanceof BeregningUtfallMerknad merknad) {
			var ikkeBeregnet = new RegelResultat(ResultatBeregningType.IKKE_BEREGNET, getRegelVersjonsNummer(), input, sporing);
			return RegelResultat.medRegelMerknad(ikkeBeregnet, new RegelMerknad(merknad.regelUtfallMerknad()));
		} else {
			throw new IllegalStateException("Utviklerfeil: Ugyldig utfall" + ev.getOutcome());
		}

	}

	private static RegelResultat opprettResultat(ResultatBeregningType beregningsresultat, String input, String sporing) {
		return new RegelResultat(beregningsresultat, getRegelVersjonsNummer(), input, sporing);
	}


}
