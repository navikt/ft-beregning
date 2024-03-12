package no.nav.folketrygdloven.beregningsgrunnlag.vurder;

import java.math.BigDecimal;

import no.nav.fpsak.nare.doc.RuleDocumentation;

@RuleDocumentation(BeregningsgrunnlagUnderEnG.ID)
class BeregningsgrunnlagUnderEnG extends SjekkBeregningsgrunnlagMindreEnnAntallG {

    static final String ID = "FT_VK_8_47_5";
	private static final BigDecimal MINIMUM_G = BigDecimal.valueOf(1);

	BeregningsgrunnlagUnderEnG() {
        super(ID, MINIMUM_G);
	}

}
