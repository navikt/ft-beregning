package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FaktaAggregatDto {

    @JsonProperty(value = "faktaArbeidsforholdListe")
    @Size()
    @Valid
    private List<FaktaArbeidsforholdDto> faktaArbeidsforholdListe = new ArrayList<>();

    @JsonProperty(value = "faktaAktør")
    @Valid
    private FaktaAktørDto faktaAktør;

    public FaktaAggregatDto() { }

    public FaktaAggregatDto(@Size() @Valid List<FaktaArbeidsforholdDto> faktaArbeidsforholdListe, @Valid FaktaAktørDto faktaAktør) {
        this.faktaArbeidsforholdListe = faktaArbeidsforholdListe;
        this.faktaAktør = faktaAktør;
    }

    public List<FaktaArbeidsforholdDto> getFaktaArbeidsforholdListe() {
        return faktaArbeidsforholdListe;
    }

    public FaktaAktørDto getFaktaAktør() {
        return faktaAktør;
    }
}
