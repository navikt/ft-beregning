package no.nav.folketrygdloven.kalkulator.input;

import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.opptjening.OpptjeningAktiviteterDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

/** Inputstruktur for beregningsgrunnlag tjenester. */
public class HåndterBeregningsgrunnlagInput extends BeregningsgrunnlagInput {

    /**
     * Tilstand som beregningsgrunnlag lagres med ut av steget (ved avklaringsbehovbekreftelse)
     */
    protected BeregningsgrunnlagTilstand håndteringTilstand;

    /** Forrige grunnlag for steg-ut-tilstand */
    private BeregningsgrunnlagGrunnlagDto forrigeGrunnlagFraHåndteringTilstand;


    public HåndterBeregningsgrunnlagInput(BeregningsgrunnlagInput input, BeregningsgrunnlagTilstand håndteringTilstand) {
        super(input);
        this.håndteringTilstand = håndteringTilstand;
    }

    public HåndterBeregningsgrunnlagInput(KoblingReferanse koblingReferanse,
                                          InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                          OpptjeningAktiviteterDto opptjeningAktiviteter,
                                          List<KravperioderPrArbeidsforholdDto> kravperioderPrArbeidsgiver,
                                          YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        super(koblingReferanse, iayGrunnlag, opptjeningAktiviteter, kravperioderPrArbeidsgiver, ytelsespesifiktGrunnlag);
    }

    protected HåndterBeregningsgrunnlagInput(HåndterBeregningsgrunnlagInput input) {
        super(input);
        this.forrigeGrunnlagFraHåndteringTilstand = input.getForrigeGrunnlagFraHåndteringTilstand().orElse(null);
        this.håndteringTilstand = input.getStegUtTilstandHvisFinnes().orElse(null);
    }

    public Optional<BeregningsgrunnlagGrunnlagDto> getForrigeGrunnlagFraHåndteringTilstand() {
        return Optional.ofNullable(forrigeGrunnlagFraHåndteringTilstand);
    }

    public Optional<BeregningsgrunnlagTilstand> getStegUtTilstandHvisFinnes() {
        return Optional.ofNullable(håndteringTilstand);
    }

    public BeregningsgrunnlagTilstand getHåndteringTilstand() {
        return håndteringTilstand;
    }

    public HåndterBeregningsgrunnlagInput medForrigeGrunnlagFraHåndtering(BeregningsgrunnlagGrunnlagDto grunnlag) {
        var newInput = new HåndterBeregningsgrunnlagInput(this);
        newInput.forrigeGrunnlagFraHåndteringTilstand = grunnlag;
        return newInput;
    }

}
