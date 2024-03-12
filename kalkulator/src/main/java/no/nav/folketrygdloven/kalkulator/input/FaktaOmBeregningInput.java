package no.nav.folketrygdloven.kalkulator.input;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class FaktaOmBeregningInput extends StegProsesseringInput {

    /**
     * Grunnbeløpsatser
     */
    private List<GrunnbeløpInput> grunnbeløpInput = new ArrayList<>();


    public FaktaOmBeregningInput(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER;
        super.stegUtTilstand = BeregningsgrunnlagTilstand.KOFAKBER_UT;
    }

    public FaktaOmBeregningInput(KoblingReferanse koblingReferanse,
                                 InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                 OpptjeningAktiviteterDto opptjeningAktiviteter,
                                 List<KravperioderPrArbeidsforholdDto> kravperioderPrArbeidsgiver,
                                 YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        super(BeregningsgrunnlagTilstand.OPPDATERT_MED_ANDELER,
                koblingReferanse, iayGrunnlag, opptjeningAktiviteter, kravperioderPrArbeidsgiver, ytelsespesifiktGrunnlag);
    }

    protected FaktaOmBeregningInput(FaktaOmBeregningInput input) {
        super(input);
        this.grunnbeløpInput = input.getGrunnbeløpInput();
    }

    public List<GrunnbeløpInput> getGrunnbeløpInput() {
        return grunnbeløpInput;
    }

    public FaktaOmBeregningInput medGrunnbeløpInput(List<GrunnbeløpInput> grunnbeløpInput) {
        var newInput = new FaktaOmBeregningInput(this);
        newInput.grunnbeløpInput = grunnbeløpInput;
        return newInput;
    }

}
