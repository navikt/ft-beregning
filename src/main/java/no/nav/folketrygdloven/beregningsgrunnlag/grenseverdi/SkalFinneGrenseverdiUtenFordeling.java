package no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.psb.PleiepengerGrunnlagFastsettGrenseverdi;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SkalFinneGrenseverdiUtenFordeling.ID)
public class SkalFinneGrenseverdiUtenFordeling extends LeafSpecification<BeregningsgrunnlagPeriode> {

	public static final String ID = "FP_BR_29.1";
	public static final String BESKRIVELSE = "Skal finne grenseverdi uten å ta hensyn til fordeling?";

	public SkalFinneGrenseverdiUtenFordeling() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

		if (!grunnlag.getBeregningsgrunnlag().getToggles().isEnabled("GRADERING_MOT_INNTEKT")) {
			return nei();
		}

		var ytelsesSpesifiktGrunnlag = grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag();

		if (ytelsesSpesifiktGrunnlag instanceof PleiepengerGrunnlagFastsettGrenseverdi pleiepengergrunnlag) {
			var startdatoNyeGraderingsregler = pleiepengergrunnlag.getStartdatoNyeGraderingsregler();
			SingleEvaluation resultat = startdatoNyeGraderingsregler != null && !grunnlag.getPeriodeFom().isBefore(startdatoNyeGraderingsregler) ? ja() : nei();
			resultat.setEvaluationProperty("startdatoNyeGraderingsregler", startdatoNyeGraderingsregler);
			resultat.setEvaluationProperty("periodeFom", grunnlag.getPeriodeFom());
			return resultat;
		} else {
			return nei();
		}

	}
}
