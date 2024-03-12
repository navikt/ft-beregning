package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.time.LocalDate;
import java.util.List;

public class FastsattePerioderTidsbegrensetDto {

    private LocalDate periodeFom;
    private LocalDate periodeTom;

    private List<FastsatteAndelerTidsbegrensetDto> fastsatteTidsbegrensedeAndeler;

    public FastsattePerioderTidsbegrensetDto(LocalDate periodeFom,
                                             LocalDate periodeTom,
                                             List<FastsatteAndelerTidsbegrensetDto> fastsatteTidsbegrensedeAndeler) {
        this.periodeFom = periodeFom;
        this.periodeTom = periodeTom;
        this.fastsatteTidsbegrensedeAndeler = fastsatteTidsbegrensedeAndeler;
    }

    public LocalDate getPeriodeFom() {
        return periodeFom;
    }

    public LocalDate getPeriodeTom() {
        return periodeTom;
    }

    public List<FastsatteAndelerTidsbegrensetDto> getFastsatteTidsbegrensedeAndeler() {
        return fastsatteTidsbegrensedeAndeler;
    }
}
