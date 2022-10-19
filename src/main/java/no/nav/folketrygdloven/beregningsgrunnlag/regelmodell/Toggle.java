package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

public class Toggle {

	private final String feature;
	private final boolean value;

	public Toggle(String feature, boolean value) {
		this.feature = feature;
		this.value = value;
	}

	public String getFeature() {
		return feature;
	}

	public boolean getValue() {
		return value;
	}
}
