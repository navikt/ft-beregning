package no.nav.folketrygdloven.kalkulator.guitjenester;

import java.math.BigDecimal;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagGUIInput;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.BeregningsgrunnlagDto;

/**
 * Interface ut fra kalkulus. Metoder i dette interfacet tas i bruk av BeregningStegTjeneste i kalkulus og fp-sak.
 *
 * Hver metode i interfacet korresponderer til et steg i beregning
 *
 */
@SuppressWarnings("unused")
public interface KalkulatorGuiInterface {

    /** Hent Dto til ekstern bruk
     *
     * @param input Input til produksjon av dto
     * @return Dto som representerer hele beregningsgrunnlaget
     */

    BeregningsgrunnlagDto lagBeregningsgrunnlagDto(BeregningsgrunnlagGUIInput input);

    /** Finn arbeidsprosenter for en andel for en tidsperiode. Foreldrepenger har egen logikk her.
     *
     * @param andel aktuell andel
     * @param grunnlag - grunnlag for aktuell ytelse
     * @param periode - tidsperiode
     * @return Liste av arbeidsprosenter
     */
    List<BigDecimal> finnArbeidsprosenterIPeriode(BeregningsgrunnlagPrStatusOgAndelDto andel, YtelsespesifiktGrunnlag grunnlag, Intervall periode);
}
