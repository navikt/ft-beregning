package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.util.List;

public class AvklarteAktiviteterDto {

    private List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList;

    public AvklarteAktiviteterDto(List<BeregningsaktivitetLagreDto> beregningsaktivitetLagreDtoList) { // NOSONAR
        this.beregningsaktivitetLagreDtoList = beregningsaktivitetLagreDtoList;
    }

    public List<BeregningsaktivitetLagreDto> getBeregningsaktivitetLagreDtoList() {
        return beregningsaktivitetLagreDtoList;
    }
}
