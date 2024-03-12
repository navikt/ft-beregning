package no.nav.folketrygdloven.kalkulator.input;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class ForeslåBeregningsgrunnlagInput extends StegProsesseringInput {

    /**
     * Grunnbeløpsatser
     */
    private List<GrunnbeløpInput> grunnbeløpInput = new ArrayList<>();

    public ForeslåBeregningsgrunnlagInput(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.FORESLÅTT;
        super.stegUtTilstand = BeregningsgrunnlagTilstand.FORESLÅTT_UT;
    }

    public List<GrunnbeløpInput> getGrunnbeløpInput() {
        return grunnbeløpInput;
    }

    public ForeslåBeregningsgrunnlagInput medGrunnbeløpInput(List<GrunnbeløpInput> grunnbeløpInput) {
        var newInput = new ForeslåBeregningsgrunnlagInput(this);
        newInput.grunnbeløpInput = grunnbeløpInput;
        return newInput;
    }

}
