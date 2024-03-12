package no.nav.folketrygdloven.kalkulus.håndtering.v1.fakta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VurderTidsbegrensetArbeidsforholdDto {


    @JsonProperty("fastsatteArbeidsforhold")
    @Valid
    @NotNull
    @Size(min = 1)
    private List<VurderteArbeidsforholdDto> fastsatteArbeidsforhold;

    public VurderTidsbegrensetArbeidsforholdDto() {
    }

    public VurderTidsbegrensetArbeidsforholdDto(@Valid @NotNull @Size(min = 1) List<VurderteArbeidsforholdDto> fastsatteArbeidsforhold) {
        this.fastsatteArbeidsforhold = fastsatteArbeidsforhold;
    }

    public List<VurderteArbeidsforholdDto> getFastsatteArbeidsforhold() {
        return fastsatteArbeidsforhold;
    }

    public void setFastsatteArbeidsforhold(List<VurderteArbeidsforholdDto> fastsatteArbeidsforhold) {
        this.fastsatteArbeidsforhold = fastsatteArbeidsforhold;
    }
}
