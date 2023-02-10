package no.nav.folketrygdloven.regelmodelloversetter;

import no.nav.fpsak.nare.evaluation.summary.EvaluationVersion;
import no.nav.fpsak.nare.evaluation.summary.NareVersion;

public class BeregningRegelmodellVersjon {

    private BeregningRegelmodellVersjon() {
    }

    public static final EvaluationVersion BEREGNING_REGELMODELL_VERSJON = NareVersion.readVersionPropertyFor("beregningsgrunnlag", "nare/beregningsgrunnlag-version.properties");

}
