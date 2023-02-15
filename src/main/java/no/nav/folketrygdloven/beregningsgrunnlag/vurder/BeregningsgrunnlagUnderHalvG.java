package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import java.math.BigDecimal;

import no.nav.fpsak.nare.doc.RuleDocumentation;

@RuleDocumentation(BeregningsgrunnlagUnderHalvG.ID)
class BeregningsgrunnlagUnderHalvG extends SjekkBeregningsgrunnlagMindreEnnAntallG {

	static final String ID = "FP_VK_32.1";
	private static final BigDecimal MINIMUM_G = new BigDecimal("0.5");

	BeregningsgrunnlagUnderHalvG() {
		super(ID, MINIMUM_G);
	}

}
