package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class VurderRepresentererStortingetDto {

    @Valid
    @JsonProperty(value = "stortingsperiodeFom")
    private LocalDate stortingsperiodeFom;

    @Valid
    @JsonProperty(value = "stortingsperiodeTom")
    private LocalDate stortingsperiodeTom;

    @Valid
    @JsonProperty(value = "representererStortinget")
    private Boolean representererStortinget;

    public VurderRepresentererStortingetDto(LocalDate stortingsperiodeFom, LocalDate stortingsperiodeTom, Boolean representererStortinget) {
        this.stortingsperiodeFom = stortingsperiodeFom;
        this.stortingsperiodeTom = stortingsperiodeTom;
        this.representererStortinget = representererStortinget;
    }

    public LocalDate getStortingsperiodeFom() {
        return stortingsperiodeFom;
    }

    public LocalDate getStortingsperiodeTom() {
        return stortingsperiodeTom;
    }

    public Boolean getRepresentererStortinget() {
        return representererStortinget;
    }
}
