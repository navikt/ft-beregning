package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class TidligereUtbetalingDto {

    @Valid
    @JsonProperty("fom")
    @NotNull
    private LocalDate fom;

    @Valid
    @JsonProperty("tom")
    private LocalDate tom;

    @Valid
    @JsonProperty("erTildeltRefusjon")
    @NotNull
    private Boolean erTildeltRefusjon;

    public TidligereUtbetalingDto() {
    }

    public TidligereUtbetalingDto(@Valid @NotNull LocalDate fom,
                                  @Valid LocalDate tom,
                                  @Valid @NotNull Boolean erTildeltRefusjon) {
        this.fom = fom;
        this.tom = tom;
        this.erTildeltRefusjon = erTildeltRefusjon;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public Boolean getErTildeltRefusjon() {
        return erTildeltRefusjon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TidligereUtbetalingDto that = (TidligereUtbetalingDto) o;
        return Objects.equals(fom, that.fom) &&
                Objects.equals(tom, that.tom) &&
                Objects.equals(erTildeltRefusjon, that.erTildeltRefusjon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fom, tom, erTildeltRefusjon);
    }
}
