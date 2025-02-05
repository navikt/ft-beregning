package no.nav.folketrygdloven.kalkulus.migrering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public class BeregningAktivitetOverstyringerMigreringDto extends BaseMigreringDto {

	@Valid
	@Size(max=100)
    private List<BeregningAktivitetOverstyringMigreringDto> overstyringer;

    public BeregningAktivitetOverstyringerMigreringDto() {
    }

    public BeregningAktivitetOverstyringerMigreringDto(List<BeregningAktivitetOverstyringMigreringDto> overstyringer) {
        this.overstyringer = overstyringer;
    }

    public List<BeregningAktivitetOverstyringMigreringDto> getOverstyringer() {
        return overstyringer;
    }
}
