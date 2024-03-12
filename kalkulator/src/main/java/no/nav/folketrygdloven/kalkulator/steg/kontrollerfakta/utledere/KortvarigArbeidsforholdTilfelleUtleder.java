package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.utledere;

import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.input.FaktaOmBeregningInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta.KortvarigArbeidsforholdTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaOmBeregningTilfelle;


/**
 * Utleder for fakta om beregning tilfeller for kortvarige/tidsbegrensede aktiviteter
 * <p>
 * Ein arbeidsaktivitet er regnet som kortvarig om den oppfyller følgende kriterier
 * - Starter før skjæringstidspunktet for beregning
 * - Slutter etter skjæringstidspunktet for beregning
 * - Har varighet under 6 måneder
 */
public class KortvarigArbeidsforholdTilfelleUtleder implements TilfelleUtleder {

    @Override
    public Optional<FaktaOmBeregningTilfelle> utled(FaktaOmBeregningInput input,
                                                    BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag) {
        BeregningsgrunnlagDto beregningsgrunnlag = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes().orElse(null);
        Objects.requireNonNull(beregningsgrunnlag, "beregningsgrunnlag");
        return KortvarigArbeidsforholdTjeneste.harKortvarigeArbeidsforholdOgErIkkeSN(beregningsgrunnlag, input.getIayGrunnlag()) ?
                Optional.of(FaktaOmBeregningTilfelle.VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD) : Optional.empty();
    }
}
