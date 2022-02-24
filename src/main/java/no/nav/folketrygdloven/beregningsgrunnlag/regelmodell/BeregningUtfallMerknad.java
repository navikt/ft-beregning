package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import no.nav.fpsak.nare.evaluation.RuleReasonRef;

public record BeregningUtfallMerknad(BeregningUtfallÅrsak regelUtfallMerknad,
                                     String textReason) implements RuleReasonRef {

	public BeregningUtfallMerknad(BeregningUtfallÅrsak regelUtfallMerknad) {
		this(regelUtfallMerknad, regelUtfallMerknad.getNavn());
	}

    @Override
    public String getReasonCode() {
        return regelUtfallMerknad.getKode();
    }

    @Override
    public String getReasonTextTemplate() {
        return textReason;
    }
}
