package no.nav.folketrygdloven.kalkulator.steg.fordeling;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.omfordeling.OmfordelBeregningsgrunnlagTjeneste;

public class FordelBeregningsgrunnlagTjenesteImpl implements FordelBeregningsgrunnlagTjeneste {

    private final OmfordelBeregningsgrunnlagTjeneste omfordelTjeneste = new OmfordelBeregningsgrunnlagTjeneste();

    @Override
    public BeregningsgrunnlagRegelResultat omfordelBeregningsgrunnlag(BeregningsgrunnlagInput input) {
        var resultatFraOmfordeling = omfordelTjeneste.omfordel(input, input.getBeregningsgrunnlag());
        return new BeregningsgrunnlagRegelResultat(resultatFraOmfordeling.getBeregningsgrunnlag(),
                resultatFraOmfordeling.getRegelsporinger().orElse(null));
    }

}
