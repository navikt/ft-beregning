package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn.Avslagsårsak;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class AvslagsårsakPrPeriodeDto {

    @Valid
    @NotNull
    @JsonProperty("fom")
    private LocalDate fom;

    @Valid
    @JsonProperty("tom")
    @NotNull
    private LocalDate tom;

    @Valid
    @JsonProperty("avslagsårsak")
    @NotNull
    private Avslagsårsak avslagsårsak;


    public AvslagsårsakPrPeriodeDto() {
        // Jackson
    }

    public AvslagsårsakPrPeriodeDto(@Valid @NotNull LocalDate fom, @Valid @NotNull LocalDate tom, @Valid @NotNull Avslagsårsak avslagsårsak) {
        this.fom = fom;
        this.tom = tom;
        this.avslagsårsak = avslagsårsak;
    }

    public LocalDate getFom() {
        return fom;
    }
    public LocalDate getTom() {
        return tom;
    }
    public Avslagsårsak getAvslagsårsak() {
        return avslagsårsak;
    }

}
