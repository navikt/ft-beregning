package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;

import java.time.LocalDate;

public interface MapInntektsgrunnlagVLTilRegel {
    Inntektsgrunnlag map(BeregningsgrunnlagInput input, LocalDate skjæringstidspunkt);
}
