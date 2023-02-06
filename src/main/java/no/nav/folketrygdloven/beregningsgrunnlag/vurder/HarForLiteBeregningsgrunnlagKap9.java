package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import java.math.BigDecimal;

import no.nav.fpsak.nare.doc.RuleDocumentation;

@RuleDocumentation(HarForLiteBeregningsgrunnlagKap9.ID)
class HarForLiteBeregningsgrunnlagKap9 extends SjekkBeregningsgrunnlagMindreEnnAntallG {

	static final String ID = "FP_VK_9_3_2";
	private static final BigDecimal MINIMUM_G = new BigDecimal("0.5");

	HarForLiteBeregningsgrunnlagKap9() {
		super(ID, MINIMUM_G, "Folketrygdloven §9-3 andre ledd");
	}

}
