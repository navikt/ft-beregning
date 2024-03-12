package no.nav.folketrygdloven.kalkulator.input;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class FastsettBeregningsaktiviteterInput extends StegProsesseringInput {

    /**
     * Grunnbeløpsatser
     */
    private List<GrunnbeløpInput> grunnbeløpInput = new ArrayList<>();


    public FastsettBeregningsaktiviteterInput(StegProsesseringInput input) {
        super(input);
        super.stegTilstand = BeregningsgrunnlagTilstand.OPPRETTET;
        super.stegUtTilstand = BeregningsgrunnlagTilstand.FASTSATT_BEREGNINGSAKTIVITETER;
    }

    public FastsettBeregningsaktiviteterInput(KoblingReferanse koblingReferanse,
                                              InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                              OpptjeningAktiviteterDto opptjeningAktiviteter,
                                              List<KravperioderPrArbeidsforholdDto> kravperioderPrArbeidsgiver,
                                              YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        super(BeregningsgrunnlagTilstand.OPPRETTET, koblingReferanse, iayGrunnlag, opptjeningAktiviteter, kravperioderPrArbeidsgiver, ytelsespesifiktGrunnlag);
    }

    protected FastsettBeregningsaktiviteterInput(FastsettBeregningsaktiviteterInput input) {
        super(input);
        this.grunnbeløpInput = input.getGrunnbeløpInput();
    }

    public List<GrunnbeløpInput> getGrunnbeløpInput() {
        return grunnbeløpInput;
    }

    public FastsettBeregningsaktiviteterInput medGrunnbeløpInput(List<GrunnbeløpInput> grunnbeløpInput) {
        var newInput = new FastsettBeregningsaktiviteterInput(this);
        newInput.grunnbeløpInput = grunnbeløpInput;
        return newInput;
    }


}
