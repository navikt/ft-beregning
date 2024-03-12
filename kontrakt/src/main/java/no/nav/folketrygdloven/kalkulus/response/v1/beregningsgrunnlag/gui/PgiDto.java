package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class PgiDto {

    @Valid
    @JsonProperty(value = "beløp")
    private Beløp beløp;

    @Valid
    @JsonProperty(value = "årstall")
    @Min(0)
    @Max(3000)
    private Integer årstall;

    public PgiDto() {
        // Jackson
    }

    public PgiDto(Beløp beløp, Integer årstall) {
        this.beløp = beløp;
        this.årstall = årstall;
    }

    public Beløp getBeløp() {
        return beløp;
    }

    public Integer getÅrstall() {
        return årstall;
    }
}
