package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AvklaringsbehovutlederVurderRefusjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

public class AvklaringsbehovutledertjenesteVurderRefusjonFP {
    // TODO: Rename filen, enten fjerne FP eller legge til ogSVP eller FOR

    public List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagInput input,
                                                                       BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = new ArrayList<>();

        if (AvklaringsbehovutlederVurderRefusjon.skalHaAvklaringsbehovVurderRefusjonskravKommetForSent(input)) {
            // TODO: Her skal det opprettes VURDER_REFUSJONSKRAV hvis ikke prod
            avklaringsbehov.add(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT));
        }

        if (AvklaringsbehovutlederVurderRefusjon.skalHaAvklaringsbehovVurderRefusjonskrav(input, periodisertMedRefusjonOgGradering)) {
            avklaringsbehov.add(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV));
        }

        return avklaringsbehov;
    }
}
