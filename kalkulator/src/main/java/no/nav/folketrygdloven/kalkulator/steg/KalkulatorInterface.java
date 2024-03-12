package no.nav.folketrygdloven.kalkulator.steg;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.input.FastsettBeregningsaktiviteterInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.ForeslåBesteberegningInput;
import no.nav.folketrygdloven.kalkulator.input.FortsettForeslåBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.StegProsesseringInput;
import no.nav.folketrygdloven.kalkulator.input.VurderRefusjonBeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningResultatAggregat;

/**
 * Interface ut fra kalkulus. Metoder i dette interfacet tas i bruk av BeregningStegTjeneste i kalkulus og fp-sak.
 *
 * Hver metode i interfacet korresponderer til et steg i beregning
 *
 */
@SuppressWarnings("unused")
public interface KalkulatorInterface {

    /** Steg 1: Fastsetter beregningsaktiviteter og forslår skjæringstidspunkt
     *
     * @param input Input til steget
     * @return Beregningresultat med avklaringsbehov og eventuelt avslag på vilkår.
     */
    BeregningResultatAggregat fastsettBeregningsaktiviteter(FastsettBeregningsaktiviteterInput input);

    /** Steg 2: Kontroller fakta for beregning
     *
     * @param input Input til steget
     * @return Resultat med avklaringsbehov og nytt beregningsgrunnlag
     */
    BeregningResultatAggregat kontrollerFaktaBeregningsgrunnlag(FaktaOmBeregningInput input);

    /** Steg 3: Foreslå beregningsgrunnlag
     *
     *  Utfører beregning i henhold til kap 8 i folketrygdloven
     *
     * @param input Input til steget
     * @return Resultat med avklaringsbehov og nytt beregningsgrunnlag
     */
    BeregningResultatAggregat foreslåBeregningsgrunnlag(ForeslåBeregningsgrunnlagInput input);

    /** Steg 4: Fortsett foreslå beregningsgrunnlag
     *
     *  Fortsetter beregning i henhold til kap 8 i folketrygdloven for
     *  statuser som er avhengig av resultatet fra steg 3 (foreslå beregningsgrunnlag)
     *
     * @param input Input til steget
     * @return Resultat med avklaringsbehov og nytt beregningsgrunnlag
     */
    BeregningResultatAggregat fortsettForeslåBeregningsgrunnlag(FortsettForeslåBeregningsgrunnlagInput input);

    /** Steg 4.5: Foreslår besteberegning
     *
     * Kun relevant for foreldrepenger. Beregner i henhold til §14-7 3. ledd
     *
     * @param input Input til steget
     * @return Nytt beregningsgrunnlag
     */
    BeregningResultatAggregat foreslåBesteberegning(ForeslåBesteberegningInput input);

    /** Steg 5 Vurderer beregningsgrunnlagsvilkåret
     *
     * @param input Input til steget
     * @return Vurdering av vilkår
     */
    BeregningResultatAggregat vurderBeregningsgrunnlagvilkår(StegProsesseringInput input);

    /** Steg 6 Vurderer tilkommet inntekt
     *
     * @param input Input til steget
     * @return Vurdering av vilkår
     */
    BeregningResultatAggregat vurderTilkommetInntekt(StegProsesseringInput input);

    /** Steg 7: Vurder refusjonskrav
     *
     * Vurderer beregningsgrunnlagvilkåret
     *
     * @param input Input til steget
     * @return Vurdering av refusjonskrav
     */
    BeregningResultatAggregat vurderRefusjonskravForBeregninggrunnlag(VurderRefusjonBeregningsgrunnlagInput input);

    /** Steg 8: Fordel beregningsgrunnlag
     *
     * @param input Input til steget
     * @return Nytt beregningsgrunnlag og avklaringsbehov
     */
    BeregningResultatAggregat fordelBeregningsgrunnlag(StegProsesseringInput input);

    /** Steg 9: Fastsett beregningsgrunnlag
     *
     * @param input Input til steget
     * @return Fastsatt beregningsgrunnlag
     */
    BeregningResultatAggregat fastsettBeregningsgrunnlag(StegProsesseringInput input);
}
