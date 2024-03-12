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
public class VurderteArbeidsforholdDto  {

    @JsonProperty("andelsnr")
    @Valid
    @NotNull
    @Min(1)
    @Max(100)
    private Long andelsnr;

    @JsonProperty("tidsbegrensetArbeidsforhold")
    @Valid
    @NotNull
    private Boolean tidsbegrensetArbeidsforhold;

    public VurderteArbeidsforholdDto() {
    }

    public VurderteArbeidsforholdDto(@Valid @NotNull @Min(1) @Max(100) Long andelsnr, @Valid @NotNull Boolean tidsbegrensetArbeidsforhold) {
        this.andelsnr = andelsnr;
        this.tidsbegrensetArbeidsforhold = tidsbegrensetArbeidsforhold;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public boolean isTidsbegrensetArbeidsforhold() {
        return tidsbegrensetArbeidsforhold;
    }

}
