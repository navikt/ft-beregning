package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class KortvarigeArbeidsforholdDto extends FaktaOmBeregningAndelDto {

    @Valid
    @JsonProperty(value = "erTidsbegrensetArbeidsforhold")
    private Boolean erTidsbegrensetArbeidsforhold;

    public KortvarigeArbeidsforholdDto() {
        // Hibernate
    }

    public Boolean getErTidsbegrensetArbeidsforhold() {
        return erTidsbegrensetArbeidsforhold;
    }

    public void setErTidsbegrensetArbeidsforhold(Boolean erTidsbegrensetArbeidsforhold) {
        this.erTidsbegrensetArbeidsforhold = erTidsbegrensetArbeidsforhold;
    }

}
