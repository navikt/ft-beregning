package no.nav.folketrygdloven.kalkulus.migrering;

import java.util.List;

public class BeregningAktivitetOverstyringerMigreringDto extends BaseMigreringDto {

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
