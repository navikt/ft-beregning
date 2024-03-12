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
public class AvklarAktiviteterDto {

    @Valid
    @JsonProperty(value = "aktiviteterTomDatoMapping")
    @Size(max=100)
    private List<AktivitetTomDatoMappingDto> aktiviteterTomDatoMapping;

    @Valid
    @NotNull
    @JsonProperty(value = "skjæringstidspunkt")
    private LocalDate skjæringstidspunkt;

    public List<AktivitetTomDatoMappingDto> getAktiviteterTomDatoMapping() {
        return aktiviteterTomDatoMapping;
    }

    public void setAktiviteterTomDatoMapping(List<AktivitetTomDatoMappingDto> aktiviteterTomDatoMapping) {
        this.aktiviteterTomDatoMapping = aktiviteterTomDatoMapping;
    }

    public void setSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
        this.skjæringstidspunkt = skjæringstidspunkt;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

}
