package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.AvklaringsbehovutlederVurderRefusjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

public class AvklaringsbehovutledertjenesteVurderRefusjonFP {

    public List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagInput input,
                                                                       BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        List<BeregningAvklaringsbehovResultat> avklaringsbehov = new ArrayList<>();

        if (AvklaringsbehovutlederVurderRefusjon.skalHaAvklaringsbehovVurderRefusjonskrav(input, periodisertMedRefusjonOgGradering)) {
            avklaringsbehov.add(BeregningAvklaringsbehovResultat.opprettFor(AvklaringsbehovDefinisjon.VURDER_REFUSJONSKRAV));
        }

        return avklaringsbehov;
    }
}
