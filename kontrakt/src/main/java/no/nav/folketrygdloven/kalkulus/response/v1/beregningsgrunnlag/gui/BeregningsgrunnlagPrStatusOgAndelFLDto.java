package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPrStatusOgAndelFLDto extends BeregningsgrunnlagPrStatusOgAndelDto {

    @Valid
    @JsonProperty(value = "erNyoppstartet")
    private Boolean erNyoppstartet;

    public BeregningsgrunnlagPrStatusOgAndelFLDto() {
        super();
    }

    public Boolean getErNyoppstartet() {
        return erNyoppstartet;
    }

    public void setErNyoppstartet(Boolean erNyoppstartet) {
        this.erNyoppstartet = erNyoppstartet;
    }

}
