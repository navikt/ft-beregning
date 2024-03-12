package no.nav.folketrygdloven.kalkulator.steg.foreslå;

import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon.FASTSETT_BG_AT_FL;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon.FASTSETT_BG_SN_NY_I_ARB_LIVT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon.FASTSETT_BG_TB_ARB;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon.VURDER_VARIG_ENDRT_ARB_SITSJN_MDL_INAKTV;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon.VURDER_VARIG_ENDRT_NYOPPSTR_NAERNG_SN;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelMerknad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

public class AvklaringsbehovUtlederForeslåBeregning {

    private AvklaringsbehovUtlederForeslåBeregning() {
        // Skjul
    }

    public static List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(@SuppressWarnings("unused") BeregningsgrunnlagInput input,
                                                                              List<RegelResultat> regelResultater) {
        return mapRegelResultater(regelResultater);
    }

    private static List<BeregningAvklaringsbehovResultat> mapRegelResultater(List<RegelResultat> regelResultater) {
        return regelResultater.stream()
                .map(RegelResultat::merknader)
                .flatMap(Collection::stream)
                .distinct()
                .map(AvklaringsbehovUtlederForeslåBeregning::mapRegelMerknad)
                .map(BeregningAvklaringsbehovResultat::opprettFor)
                .collect(Collectors.toList());
    }

    private static AvklaringsbehovDefinisjon mapRegelMerknad(RegelMerknad regelMerknad) {
        return switch (regelMerknad.utfallÅrsak()) {
            case FASTSETT_AVVIK_OVER_25_PROSENT, FASTSETT_AVVIK_OVER_25_PROSENT_ARBEIDSTAKER, FASTSETT_AVVIK_OVER_25_PROSENT_FRILANS,
                    FASTSETT_AVVIK_TIDSBEGRENSET_ARBEIDSTAKER -> FASTSETT_BG_AT_FL;
            case FASTSETT_AVVIK_TIDSBEGRENSET -> FASTSETT_BG_TB_ARB;
            case FASTSETT_SELVSTENDIG_NY_ARBEIDSLIVET -> FASTSETT_BG_SN_NY_I_ARB_LIVT;
            case VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT -> VURDER_VARIG_ENDRT_NYOPPSTR_NAERNG_SN;
            case VARIG_ENDRING_OG_AVVIK_STØRRE_ENN_25_PROSENT_MIDLERTIDIG_INAKTIV -> VURDER_VARIG_ENDRT_ARB_SITSJN_MDL_INAKTV;
            default -> throw new IllegalArgumentException("Utviklerfeil: Uventet regelutfall " + regelMerknad.utfallÅrsak());
        };
    }
}
