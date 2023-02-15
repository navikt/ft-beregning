package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import java.math.BigDecimal;

import no.nav.fpsak.nare.doc.RuleDocumentation;

@RuleDocumentation(HarForLiteBeregningsgrunnlagFP.ID)
class HarForLiteBeregningsgrunnlagFP extends SjekkBeregningsgrunnlagMindreEnnAntallG {

	static final String ID = "FP_VK_32.1";
	private static final BigDecimal MINIMUM_G = new BigDecimal("0.5");

	HarForLiteBeregningsgrunnlagFP() {
		super(ID, MINIMUM_G, "Folketrygdloven §14-7 første ledd");
	}

}
