package no.nav.folketrygdloven.kalkulator.steg.fordeling;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;

public interface FordelBeregningsgrunnlagTjeneste {

    BeregningsgrunnlagRegelResultat omfordelBeregningsgrunnlag(BeregningsgrunnlagInput input);

}
