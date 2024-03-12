package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;

public class AvklaringsbehovutledertjenesteVurderRefusjon {

    private AvklaringsbehovutledertjenesteVurderRefusjon() {
    }

    public static List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagInput input, BeregningsgrunnlagDto periodisertMedRefusjonOgGradering) {
        return switch (input.getFagsakYtelseType()) {
            case FORELDREPENGER -> new AvklaringsbehovutledertjenesteVurderRefusjonFP().utledAvklaringsbehov(input, periodisertMedRefusjonOgGradering);
            case SVANGERSKAPSPENGER -> new AvklaringsbehovutledertjenesteVurderRefusjonSVP().utledAvklaringsbehov(input, periodisertMedRefusjonOgGradering);
            case PLEIEPENGER_SYKT_BARN, OPPLÆRINGSPENGER, PLEIEPENGER_NÆRSTÅENDE, OMSORGSPENGER ->
                new AvklaringsbehovutledertjenesteVurderRefusjonK9().utledAvklaringsbehov(input, periodisertMedRefusjonOgGradering);
            default -> throw new IllegalStateException("Fant ikke AksjonspunkutledertjenesteVurderRefusjon for ytelsetype " + input.getFagsakYtelseType().getKode());
        };
    }


}
