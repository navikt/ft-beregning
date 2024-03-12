package no.nav.folketrygdloven.kalkulator.output;


import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagRegelType;

public record RegelSporingGrunnlag(String regelEvaluering, String regelInput,
                                   BeregningsgrunnlagRegelType regelType, String regelVersjon) {
}
