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
		var ytelsesSpesifiktGrunnlag = grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag();
		if (ytelsesSpesifiktGrunnlag instanceof PleiepengerGrunnlagFastsettGrenseverdi pleiepengergrunnlag) {
			var startdatoNyeGraderingsregler = pleiepengergrunnlag.getStartdatoNyeGraderingsregler();
			var skalKjøreMedFordeling = startdatoNyeGraderingsregler == null || grunnlag.getPeriodeFom().isBefore(startdatoNyeGraderingsregler);
			SingleEvaluation resultat = skalKjøreMedFordeling ? nei() : ja();
			resultat.setEvaluationProperty("startdatoNyeGraderingsregler", startdatoNyeGraderingsregler);
			resultat.setEvaluationProperty("periodeFom", grunnlag.getPeriodeFom());

			if (skalKjøreMedFordeling && grunnlag.getTilkommetInntektsforholdListe() != null && !grunnlag.getTilkommetInntektsforholdListe().isEmpty()) {
				throw new IllegalStateException("Hadde tilkommet inntekt satt i periode for gamle regler");
			}

			return resultat;
		} else {
			return nei();
		}

	}
}
