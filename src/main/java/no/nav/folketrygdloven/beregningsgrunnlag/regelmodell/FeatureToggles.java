package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import java.util.ArrayList;
import java.util.List;

public class FeatureToggles {

	private final List<Toggle> toggles = new ArrayList<>();

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
