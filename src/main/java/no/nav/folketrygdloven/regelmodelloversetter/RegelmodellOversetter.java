package no.nav.folketrygdloven.regelmodelloversetter;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.summary.NareVersion;

public class RegelmodellOversetter {
	private static final RegelmodellOversetterImpl OVERSETTER = new RegelmodellOversetterImpl(NareVersion.NARE_VERSION, RegelmodellVersjon.REGELMODELL_VERSJON);

	private RegelmodellOversetter() {
	}

	public static RegelResultat getRegelResultat(Evaluation evaluation, String regelInput) {
		return OVERSETTER.getRegelResultat(evaluation, regelInput);
	}

	public static String getSporing(Evaluation evaluation) {
		return OVERSETTER.getSporing(evaluation);
	}


}
