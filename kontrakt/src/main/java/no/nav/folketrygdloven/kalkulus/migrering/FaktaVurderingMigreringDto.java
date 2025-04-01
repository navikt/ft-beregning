package no.nav.folketrygdloven.kalkulus.migrering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;

public class FaktaVurderingMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
    private Boolean vurdering;

    @Valid
    @NotNull
    private FaktaVurderingKilde kilde;

	public FaktaVurderingMigreringDto() {
	}

	public FaktaVurderingMigreringDto(Boolean vurdering, FaktaVurderingKilde kilde) {
        this.vurdering = vurdering;
        this.kilde = kilde;
    }

    public Boolean getVurdering() {
        return vurdering;
    }

    public FaktaVurderingKilde getKilde() {
        return kilde;
    }
}
