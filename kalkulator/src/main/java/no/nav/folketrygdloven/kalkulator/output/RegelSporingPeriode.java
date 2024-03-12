package no.nav.folketrygdloven.kalkulator.output;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;

public record RegelSporingPeriode(String regelEvaluering, String regelInput, Intervall periode,
                                 BeregningsgrunnlagPeriodeRegelType regelType, String regelVersjon) {
}
