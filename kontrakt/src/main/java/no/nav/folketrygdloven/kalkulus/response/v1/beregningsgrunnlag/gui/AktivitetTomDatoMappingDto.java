package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
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
public class AktivitetTomDatoMappingDto {

    @Valid
    @JsonProperty(value = "tom")
    @NotNull
    private LocalDate tom;

    @Valid
    @Size(max=100)
    @JsonProperty(value = "aktiviteter")
    @NotNull
    private List<BeregningAktivitetDto> aktiviteter;

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public List<BeregningAktivitetDto> getAktiviteter() {
        return aktiviteter;
    }

    public void setAktiviteter(List<BeregningAktivitetDto> aktiviteter) {
        this.aktiviteter = aktiviteter;
    }
}
