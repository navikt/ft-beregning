package no.nav.folketrygdloven.kalkulator.input;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class FortsettForeslåBeregningsgrunnlagInput extends StegProsesseringInput {

    /**
     * Grunnbeløpsatser
     */
    private List<GrunnbeløpInput> grunnbeløpInput = new ArrayList<>();

    public FortsettForeslåBeregningsgrunnlagInput(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.FORESLÅTT_DEL_2;
        super.stegUtTilstand = BeregningsgrunnlagTilstand.FORESLÅTT_DEL_2_UT;
    }

    public List<GrunnbeløpInput> getGrunnbeløpInput() {
        return grunnbeløpInput;
    }

    public FortsettForeslåBeregningsgrunnlagInput medGrunnbeløpInput(List<GrunnbeløpInput> grunnbeløpInput) {
        var newInput = new FortsettForeslåBeregningsgrunnlagInput(this);
        newInput.grunnbeløpInput = grunnbeløpInput;
        return newInput;
    }

}
