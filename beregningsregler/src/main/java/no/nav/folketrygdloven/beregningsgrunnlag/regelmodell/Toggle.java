package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import java.util.Objects;

public class Toggle {

	private final String feature;
	private final boolean value;


	public Toggle(Toggle kopi) {
		this.feature = kopi.feature;
		this.value = kopi.value;
	}

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
        var toggle = (Toggle) o;
		return feature.equals(toggle.feature);
	}

	@Override
	public int hashCode() {
		return Objects.hash(feature);
	}
}
