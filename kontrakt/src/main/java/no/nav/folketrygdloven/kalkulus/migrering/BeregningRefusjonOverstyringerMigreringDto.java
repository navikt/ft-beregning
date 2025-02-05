package no.nav.folketrygdloven.kalkulus.migrering;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BeregningRefusjonOverstyringerMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
    @Size(max=100)
    private List<BeregningRefusjonOverstyringMigreringDto> overstyringer;

    public BeregningRefusjonOverstyringerMigreringDto() {
    }

    public BeregningRefusjonOverstyringerMigreringDto(List<BeregningRefusjonOverstyringMigreringDto> overstyringer) {
        this.overstyringer = overstyringer;
    }

    public List<BeregningRefusjonOverstyringMigreringDto> getOverstyringer() {
        return overstyringer;
    }
}
