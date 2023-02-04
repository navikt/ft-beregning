package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import java.math.BigDecimal;

import no.nav.fpsak.nare.doc.RuleDocumentation;

@RuleDocumentation(HarForLiteBeregningsgrunnlagAktiv.ID)
class HarForLiteBeregningsgrunnlagAktiv extends SjekkBeregningsgrunnlagMindreEnnAntallG {

	static final String ID = "FP_VK_32.1";
	private static final BigDecimal MINIMUM_G = new BigDecimal("0.5");

	HarForLiteBeregningsgrunnlagAktiv() {
		super(ID, MINIMUM_G, "Folketrygdloven §8-3 andre ledd");
	}

}
