package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;

public interface MapBeregningAktiviteterFraVLTilRegel {
    AktivitetStatusModell mapForSkjæringstidspunkt(FastsettBeregningsaktiviteterInput input);
}
