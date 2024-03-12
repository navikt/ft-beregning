package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FastsatteAndelerTidsbegrensetDto {

    @JsonProperty("andelsnr")
    @Valid
    @Min(1)
    @Max(100)
    private Long andelsnr;

    @JsonProperty("bruttoFastsattInntekt")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer bruttoFastsattInntekt;

    public FastsatteAndelerTidsbegrensetDto() {
    }

    public FastsatteAndelerTidsbegrensetDto(@Valid @NotNull Long andelsnr, @Valid @NotNull Integer bruttoFastsattInntekt) {
        this.andelsnr = andelsnr;
        this.bruttoFastsattInntekt = bruttoFastsattInntekt;
    }

    public Long getAndelsnr() { return andelsnr; }

    public Integer getBruttoFastsattInntekt() {
        return bruttoFastsattInntekt;
    }

}
