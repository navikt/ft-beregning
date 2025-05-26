package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.time.LocalDate;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;

public interface MapInntektsgrunnlagVLTilRegel {
    Inntektsgrunnlag mapInntektsgrunnlagFørStpBeregning(BeregningsgrunnlagInput input, LocalDate skjæringstidspunkt);
}
