package no.nav.folketrygdloven.kalkulus.håndtering.v1.foreslå;

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
public class InntektPrAndelDto {

    @JsonProperty("inntekt")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer inntekt;

    @JsonProperty("andelsnr")
    @Valid
    @Min(1)
    @Max(100)
    private Long andelsnr;

    public InntektPrAndelDto() {
        //def ctc
    }

    public InntektPrAndelDto(@JsonProperty("inntekt") @Valid @NotNull Integer inntekt, @JsonProperty("andelsnr") @Valid @NotNull Long andelsnr) {
        this.inntekt = inntekt;
        this.andelsnr = andelsnr;
    }

    public Integer getInntekt() {
        return inntekt;
    }

    public void setInntekt(Integer inntekt) {
        this.inntekt = inntekt;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public void setAndelsnr(Long andelsnr) {
        this.andelsnr = andelsnr;
    }
}
