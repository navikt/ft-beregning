package no.nav.folketrygdloven.kalkulus.migrering;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class BeregningRefusjonOverstyringerMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
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
