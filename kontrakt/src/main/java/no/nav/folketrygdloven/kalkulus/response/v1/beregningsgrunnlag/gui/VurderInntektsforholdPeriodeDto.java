package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;


import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class VurderInntektsforholdPeriodeDto {

    @Valid
    @JsonProperty(value = "fom")
    @NotNull
    private LocalDate fom;

    @Valid
    @JsonProperty(value = "tom")
    @NotNull
    private LocalDate tom;

    @Valid
    @JsonProperty(value = "inntektsforholdListe")
    @Size(max = 50)
    @NotNull
    private List<InntektsforholdDto> inntektsforholdListe = new ArrayList<>();

    public VurderInntektsforholdPeriodeDto() {
    }

    public VurderInntektsforholdPeriodeDto(LocalDate fom, LocalDate tom, List<InntektsforholdDto> inntektsforholdListe) {
        this.fom = fom;
        this.tom = tom;
        this.inntektsforholdListe = inntektsforholdListe;
    }

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public List<InntektsforholdDto> getInntektsforholdListe() {
        return inntektsforholdListe;
    }

    public void setInntektsforholdListe(List<InntektsforholdDto> inntektsforholdListe) {
        this.inntektsforholdListe = inntektsforholdListe;
    }

}
