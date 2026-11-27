package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.util.List;

public class OverstyrRefusjonDto {

    private final List<RefusjonOverstyringAndel> overstyrteAndeler;

    public OverstyrRefusjonDto(List<RefusjonOverstyringAndel> overstyrteAndeler) {
        this.overstyrteAndeler = overstyrteAndeler;
    }

    public List<RefusjonOverstyringAndel> getOverstyrteAndeler() {
        return overstyrteAndeler;
    }
}
