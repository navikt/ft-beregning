package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fakta;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class Saksopplysninger {

    @Valid
    @JsonProperty(value = "arbeidsforholdMedLønnsendring")
    @Size(max=100)
    private List<ArbeidsforholdDto> arbeidsforholdMedLønnsendring;

    @Valid
    @JsonProperty(value = "lønnsendringSaksopplysning")
    @Size(max=100)
    private List<LønnsendringSaksopplysningDto> lønnsendringSaksopplysning;

    @Valid
    @JsonProperty(value = "kortvarigeArbeidsforhold")
    @Size(max=100)
    private List<ArbeidsforholdDto> kortvarigeArbeidsforhold;

    public List<ArbeidsforholdDto> getArbeidsforholdMedLønnsendring() {
        return arbeidsforholdMedLønnsendring;
    }

    public void setArbeidsforholdMedLønnsendring(List<ArbeidsforholdDto> arbeidsforholdMedLønnsendring) {
        this.arbeidsforholdMedLønnsendring = arbeidsforholdMedLønnsendring;
    }

    public List<ArbeidsforholdDto> getKortvarigeArbeidsforhold() {
        return kortvarigeArbeidsforhold;
    }

    public void setKortvarigeArbeidsforhold(List<ArbeidsforholdDto> kortvarigeArbeidsforhold) {
        this.kortvarigeArbeidsforhold = kortvarigeArbeidsforhold;
    }

    public List<LønnsendringSaksopplysningDto> getLønnsendringSaksopplysning() {
        return lønnsendringSaksopplysning;
    }

    public void setLønnsendringSaksopplysning(List<LønnsendringSaksopplysningDto> lønnsendringSaksopplysning) {
        this.lønnsendringSaksopplysning = lønnsendringSaksopplysning;
    }
}
