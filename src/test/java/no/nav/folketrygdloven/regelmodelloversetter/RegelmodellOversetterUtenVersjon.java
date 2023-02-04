package no.nav.folketrygdloven.regelmodelloversetter;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.fpsak.nare.evaluation.Evaluation;

public class RegelmodellOversetterUtenVersjon {

	private static final RegelmodellOversetterImpl OVERSETTER = RegelmodellOversetterImpl.utenVersjoner();

    private RegelmodellOversetterUtenVersjon() {
    }

    public static RegelResultat getRegelResultat(Evaluation evaluation, String regelInput) {
		return OVERSETTER.getRegelResultat(evaluation, regelInput);
    }

    public static String getSporing(Evaluation evaluation) {
		return OVERSETTER.getSporing(evaluation);
    }

}
