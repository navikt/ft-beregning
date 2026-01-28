package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AvklaringsbehovutlederVurderRefusjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

public class AvklaringsbehovutledertjenesteVurderRefusjon {

    private AvklaringsbehovutledertjenesteVurderRefusjon() {
        // Skjuler default
    }

    public static List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagInput input,
                                                                              BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        return AvklaringsbehovutlederVurderRefusjon.skalHaAvklaringsbehovVurderRefusjonskrav(input, periodisertMedRefusjonOgGradering) ? List.of(
            BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.
                VURDER_REFUSJONSKRAV)) : List.of();
    }
}
