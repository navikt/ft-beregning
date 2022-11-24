package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FeatureToggles {

	private Set<Toggle> toggles = new HashSet<>();

	public FeatureToggles() {
	}

	public FeatureToggles(FeatureToggles kopi) {
		this.toggles = kopi.toggles.stream().map(Toggle::new).collect(Collectors.toSet());
	}

	public void leggTilToggle(Toggle toggle) {
		toggles.add(toggle);
	}

	public boolean isEnabled(String feature) {
		return toggles.stream().filter(t -> t.getFeature().equals(feature))
				.findFirst().map(Toggle::getValue).orElse(false);
	}


	public boolean isEnabled(String feature, boolean defaultValue) {
		return toggles.stream().filter(t -> t.getFeature().equals(feature))
				.findFirst().map(Toggle::getValue).orElse(defaultValue);
	}

}
