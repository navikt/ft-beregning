package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FastsettMånedsinntektFLDto {

    @JsonProperty("maanedsinntekt")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer maanedsinntekt;

    public FastsettMånedsinntektFLDto() {
    }

    public FastsettMånedsinntektFLDto(Integer maanedsInntekt) { // NOSONAR
        this.maanedsinntekt = maanedsInntekt;
    }

    public void setMaanedsinntekt(Integer maanedsinntekt) {
        this.maanedsinntekt = maanedsinntekt;
    }

    public Integer getMaanedsinntekt() {
        return maanedsinntekt;
    }
}
