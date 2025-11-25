package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.util.ArrayList;
import java.util.List;

public class FastsettBGTidsbegrensetArbeidsforholdDto {

    private List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder;
    private Integer frilansInntekt;

    FastsettBGTidsbegrensetArbeidsforholdDto() {
        // For Jackson
    }

    public FastsettBGTidsbegrensetArbeidsforholdDto(List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder, Integer frilansInntekt) { // NOSONAR
        this.fastsatteTidsbegrensedePerioder = new ArrayList<>(fastsatteTidsbegrensedePerioder);
        this.frilansInntekt = frilansInntekt;
    }

    public List<FastsattePerioderTidsbegrensetDto> getFastsatteTidsbegrensedePerioder() {
        return fastsatteTidsbegrensedePerioder;
    }

    public Integer getFrilansInntekt() {
        return frilansInntekt;
    }
}
