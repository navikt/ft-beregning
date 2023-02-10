package no.nav.folketrygdloven.beregningsgrunnlag;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.fpsak.nare.evaluation.Evaluation;

/**
 * bruk istedet no.nav.folketrygdloven.regelmodelloversetter.RegelmodellOversetter
 * <p>
 * flyttes da denne brukes for flere regler, ikke kun beregningsgrunnlag
 */
@Deprecated(forRemoval = true, since = "3.3.0")
public class RegelmodellOversetter {

	private RegelmodellOversetter() {
	}

	public static RegelResultat getRegelResultat(Evaluation evaluation, String regelInput) {
		return no.nav.folketrygdloven.regelmodelloversetter.RegelmodellOversetter.getRegelResultat(evaluation, regelInput);
	}

	@Deprecated(forRemoval = true) // Hent fra RegelResultat sin sporing
	public static String getSporing(Evaluation evaluation) {
		return no.nav.folketrygdloven.regelmodelloversetter.RegelmodellOversetter.getSporing(evaluation);
	}
}
