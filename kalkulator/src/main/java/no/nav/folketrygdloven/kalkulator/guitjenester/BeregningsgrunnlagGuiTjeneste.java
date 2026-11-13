package no.nav.folketrygdloven.kalkulator.guitjenester;

import java.math.BigDecimal;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.ForeldrepengerGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlag;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.response.beregningsgrunnlag.gui.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui.FordelingGrunnlagDto;

public class BeregningsgrunnlagGuiTjeneste implements KalkulatorGuiInterface {

    private static final BeregningsgrunnlagDtoTjeneste BEREGNINGSGRUNNLAG_DTO_TJENESTE = new BeregningsgrunnlagDtoTjeneste();
    private static final FordelingsgrunnlagDtoTjeneste FORDELINGSGRUNNLAG_DTO_TJENESTE = new FordelingsgrunnlagDtoTjeneste();
    private static final FinnArbeidsprosenter ARBEIDSPROSENT_FP = new FinnArbeidsprosenterFP();
    private static final FinnArbeidsprosenter ARBEIDSPROSENT_UTBGRAD = new FinnArbeidsprosenterUtbetalingsgrad();

    @Override
    public BeregningsgrunnlagDto lagBeregningsgrunnlagDto(BeregningsgrunnlagGUIInput input) {
        return BEREGNINGSGRUNNLAG_DTO_TJENESTE.lagBeregningsgrunnlagDto(input);
    }

    @Override
    public FordelingGrunnlagDto lagFordelingsgrunnlagDto(FordelingGrunnlag input) {
        return FORDELINGSGRUNNLAG_DTO_TJENESTE.lagFordelingsgrunnlagDto(input);
    }

    @Override
    public List<BigDecimal> finnArbeidsprosenterIPeriode(BeregningsgrunnlagPrStatusOgAndelDto andel, YtelsespesifiktGrunnlag grunnlag, Intervall periode) {
        if (grunnlag instanceof ForeldrepengerGrunnlag) {
            return ARBEIDSPROSENT_FP.finnArbeidsprosenterIPeriode(andel, grunnlag, periode);
        } else {
            return ARBEIDSPROSENT_UTBGRAD.finnArbeidsprosenterIPeriode(andel, grunnlag, periode);
        }
    }

}
