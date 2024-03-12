package no.nav.folketrygdloven.kalkulator.avklaringsbehov.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class FastsettBGTidsbegrensetArbeidsforholdDto {


    @Valid
    @Size(max = 100)
    private List<FastsattePerioderTidsbegrensetDto> fastsatteTidsbegrensedePerioder;

    @Min(0)
    @Max(178956970)
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
