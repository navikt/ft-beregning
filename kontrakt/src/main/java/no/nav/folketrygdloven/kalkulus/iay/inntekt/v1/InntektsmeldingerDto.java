package no.nav.folketrygdloven.kalkulus.iay.inntekt.v1;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.ALWAYS, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class InntektsmeldingerDto {

    @JsonProperty(value = "inntektsmeldinger")
    @Valid
    @Size(min = 1)
    private List<InntektsmeldingDto> inntektsmeldinger;

    public InntektsmeldingerDto() {
        // default ctor
    }

    public InntektsmeldingerDto(List<InntektsmeldingDto> inntektsmeldinger) {
        this.inntektsmeldinger = inntektsmeldinger;
    }

    public List<InntektsmeldingDto> getInntektsmeldinger() {
        return inntektsmeldinger;
    }
}
