package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import java.math.BigDecimal;

import no.nav.fpsak.nare.doc.RuleDocumentation;

@RuleDocumentation(HarForLiteBeregningsgrunnlagMidlertidigInaktiv.ID)
class HarForLiteBeregningsgrunnlagMidlertidigInaktiv extends SjekkBeregningsgrunnlagMindreEnnAntallG {

    static final String ID = "FP_VK_47.4";
	private static final BigDecimal MINIMUM_G = BigDecimal.valueOf(1);

	HarForLiteBeregningsgrunnlagMidlertidigInaktiv() {
        super(ID, MINIMUM_G, "Folketrygdloven §8-47 4.ledd");
	}

}
