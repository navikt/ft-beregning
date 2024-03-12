package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.frisinn;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class OpplystPeriodeDto {

    @Valid
    @NotNull
    @JsonProperty("fom")
    private LocalDate fom;

    @Valid
    @JsonProperty("tom")
    @NotNull
    private LocalDate tom;

    @Valid
    @JsonProperty("statusSøktFor")
    @NotNull
    private AktivitetStatus statusSøktFor;


    public OpplystPeriodeDto() {
        // Jackson
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

    public AktivitetStatus getStatusSøktFor() {
        return statusSøktFor;
    }

    public void setStatusSøktFor(AktivitetStatus statusSøktFor) {
        this.statusSøktFor = statusSøktFor;
    }
}
