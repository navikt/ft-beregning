package no.nav.folketrygdloven.kalkulator.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class VurderRefusjonBeregningsgrunnlagInput extends StegProsesseringInput {


    /**
     * Uregulert grunnbeløp om det finnes beregningsgrunnlag som ble fastsatt etter 1. mai.
     *
     * En G-regulering ikke skal påvirke utfallet av beregningsgrunnlagvilkåret (se TFP-3599 og https://confluence.adeo.no/display/TVF/G-regulering)
     */
    private Beløp uregulertGrunnbeløp;

    /** Grunnlag for Beregningsgrunnlg opprettet eller modifisert av modulen i original behandling. Settes på av modulen. */
    private List<BeregningsgrunnlagGrunnlagDto> beregningsgrunnlagGrunnlagFraForrigeBehandling = new ArrayList<>();


    public VurderRefusjonBeregningsgrunnlagInput(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.VURDERT_REFUSJON;
        super.stegUtTilstand = BeregningsgrunnlagTilstand.VURDERT_REFUSJON_UT;
        if (input instanceof VurderRefusjonBeregningsgrunnlagInput) {
            VurderRefusjonBeregningsgrunnlagInput vurderRefInput = (VurderRefusjonBeregningsgrunnlagInput) input;
            beregningsgrunnlagGrunnlagFraForrigeBehandling = vurderRefInput.getBeregningsgrunnlagGrunnlagFraForrigeBehandling();
        }
    }


    public Optional<Beløp> getUregulertGrunnbeløp() {
        return Optional.ofNullable(uregulertGrunnbeløp);
    }


    public VurderRefusjonBeregningsgrunnlagInput medUregulertGrunnbeløp(Beløp uregulertGrunnbeløp) {
        var newInput = new VurderRefusjonBeregningsgrunnlagInput(this);
        newInput.uregulertGrunnbeløp = uregulertGrunnbeløp;
        return newInput;
    }

    // Brukes av fp-sak
    public VurderRefusjonBeregningsgrunnlagInput medBeregningsgrunnlagGrunnlagFraForrigeBehandling(BeregningsgrunnlagGrunnlagDto grunnlag) {
        var newInput = new VurderRefusjonBeregningsgrunnlagInput(this);
        newInput.beregningsgrunnlagGrunnlagFraForrigeBehandling = List.of(grunnlag);
        return newInput;
    }

    public VurderRefusjonBeregningsgrunnlagInput medBeregningsgrunnlagGrunnlagFraForrigeBehandling(List<BeregningsgrunnlagGrunnlagDto> beregningsgrunnlagGrunnlagFraForrigeBehandling) {
        var newInput = new VurderRefusjonBeregningsgrunnlagInput(this);
        newInput.beregningsgrunnlagGrunnlagFraForrigeBehandling = beregningsgrunnlagGrunnlagFraForrigeBehandling;
        return newInput;
    }

    public List<BeregningsgrunnlagGrunnlagDto> getBeregningsgrunnlagGrunnlagFraForrigeBehandling() {
        return beregningsgrunnlagGrunnlagFraForrigeBehandling;
    }

}
