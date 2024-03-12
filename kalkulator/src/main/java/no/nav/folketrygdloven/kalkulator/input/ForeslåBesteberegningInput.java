package no.nav.folketrygdloven.kalkulator.input;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class ForeslåBesteberegningInput extends StegProsesseringInput {

    /**
     * Grunnbeløpsatser
     */
    private List<GrunnbeløpInput> grunnbeløpInput = new ArrayList<>();

    public ForeslåBesteberegningInput(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.BESTEBEREGNET;
    }

    public List<GrunnbeløpInput> getGrunnbeløpInput() {
        return grunnbeløpInput;
    }

    public ForeslåBesteberegningInput medGrunnbeløpInput(List<GrunnbeløpInput> grunnbeløpInput) {
        var newInput = new ForeslåBesteberegningInput(this);
        newInput.grunnbeløpInput = grunnbeløpInput;
        return newInput;
    }

}
