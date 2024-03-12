package no.nav.folketrygdloven.kalkulator.input;

import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

/**
 * Inputstruktur for beregningsgrunnlag tjenester.
 */
public class StegProsesseringInput extends BeregningsgrunnlagInput {

    /**
     * Tilstand som beregningsgrunnlag lagres med inne i steget
     */
    protected BeregningsgrunnlagTilstand stegTilstand;

    /**
     * Tilstand som beregningsgrunnlag lagres med ut av steget (ved avklaringsbehovbekreftelse)
     */
    protected BeregningsgrunnlagTilstand stegUtTilstand;

    /**
     * Forrige grunnlag for steg-ut-tilstand
     */
    private BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraStegUt;

    /**
     * Forrige grunnlag for steg-tilstand
     */
    private BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraSteg;


    /**
     * Grunnlag for steg-tilstand fra original behandling som har samme skjæringstidspunkt som grunnlaget til behandling
     */
    private BeregningsgrunnlagGrunnlagDto originalGrunnlagFraSteg;

    public StegProsesseringInput(BeregningsgrunnlagInput input, BeregningsgrunnlagTilstand stegTilstand) {
        super(input);
        this.stegTilstand = stegTilstand;
    }

    public StegProsesseringInput(BeregningsgrunnlagTilstand stegTilstand,
                                 KoblingReferanse koblingReferanse,
                                 InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                 OpptjeningAktiviteterDto opptjeningAktiviteter,
                                 List<KravperioderPrArbeidsforholdDto> kravperioderPrArbeidsgiver,
                                 YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        super(koblingReferanse, iayGrunnlag, opptjeningAktiviteter, kravperioderPrArbeidsgiver, ytelsespesifiktGrunnlag);
        this.stegTilstand = stegTilstand;
    }

    protected StegProsesseringInput(StegProsesseringInput input) {
        super(input);
        this.forrigeGrunnlagFraSteg = input.getForrigeGrunnlagFraSteg().orElse(null);
        this.forrigeGrunnlagFraStegUt = input.getForrigeGrunnlagFraStegUt().orElse(null);
        this.originalGrunnlagFraSteg = input.getOriginalGrunnlagFraSteg().orElse(null);
        this.stegTilstand = input.getStegTilstand();
        this.stegUtTilstand = input.getStegUtTilstandHvisFinnes().orElse(null);
    }

    public Optional<BeregningsgrunnlagGrunnlagDto> getForrigeGrunnlagFraStegUt() {
        return Optional.ofNullable(forrigeGrunnlagFraStegUt);
    }

    public Optional<BeregningsgrunnlagGrunnlagDto> getForrigeGrunnlagFraSteg() {
        return Optional.ofNullable(forrigeGrunnlagFraSteg);
    }


    public Optional<BeregningsgrunnlagGrunnlagDto> getOriginalGrunnlagFraSteg() {
        return Optional.ofNullable(originalGrunnlagFraSteg);
    }

    public BeregningsgrunnlagTilstand getStegTilstand() {
        return stegTilstand;
    }

    public Optional<BeregningsgrunnlagTilstand> getStegUtTilstandHvisFinnes() {
        return Optional.ofNullable(stegUtTilstand);
    }

    public BeregningsgrunnlagTilstand getStegUtTilstand() {
        if (stegUtTilstand == null) {
            throw new IllegalStateException("Steg ut tilstand er ikke tilgjengelig for steg.");
        }
        return stegUtTilstand;
    }


    public StegProsesseringInput medForrigeGrunnlagFraStegUt(BeregningsgrunnlagGrunnlagDto grunnlag) {
        var newInput = new StegProsesseringInput(this);
        newInput.forrigeGrunnlagFraStegUt = grunnlag;
        return newInput;
    }

    public StegProsesseringInput medForrigeGrunnlagFraSteg(BeregningsgrunnlagGrunnlagDto grunnlag) {
        var newInput = new StegProsesseringInput(this);
        newInput.forrigeGrunnlagFraSteg = grunnlag;
        return newInput;
    }

    public StegProsesseringInput medOriginalGrunnlagFraSteg(BeregningsgrunnlagGrunnlagDto grunnlag) {
        var newInput = new StegProsesseringInput(this);
        newInput.originalGrunnlagFraSteg = grunnlag;
        return newInput;
    }

    public StegProsesseringInput medStegUtTilstand(BeregningsgrunnlagTilstand stegUtTilstand) {
        this.stegUtTilstand = stegUtTilstand;
        return this;
    }

}
