package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import java.util.Objects;

public record RegelMerknad(BeregningUtfallÅrsak utfallÅrsak) {

	@Deprecated // Bruk utfallÅrsak()
	public String getMerknadKode() {
        return utfallÅrsak().getKode();
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RegelMerknad)) return false;
        var that = (RegelMerknad) o;
		return utfallÅrsak == that.utfallÅrsak;
	}

	@Override
	public int hashCode() {
		return Objects.hash(utfallÅrsak);
	}
}
