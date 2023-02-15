package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import java.math.BigDecimal;

import no.nav.fpsak.nare.doc.RuleDocumentation;

@RuleDocumentation(HarForLiteBeregningsgrunnlagSVP.ID)
class HarForLiteBeregningsgrunnlagSVP extends SjekkBeregningsgrunnlagMindreEnnAntallG {

	static final String ID = "FP_VK_32.1";
	private static final BigDecimal MINIMUM_G = new BigDecimal("0.5");

	HarForLiteBeregningsgrunnlagSVP() {
		super(ID, MINIMUM_G, "§14-4 tredje ledd");
	}

}
