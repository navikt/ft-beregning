package no.nav.folketrygdloven.kalkulus.request.v1;

import java.time.LocalDate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HentGrunnbeløpRequest {

    @JsonProperty(value = "dato", required = true)
    @Valid
    @NotNull
    private LocalDate dato;


    protected HentGrunnbeløpRequest() {
        // default ctor
    }

    public HentGrunnbeløpRequest(@Valid @NotNull LocalDate dato) {
        this.dato = dato;
    }

    public LocalDate getDato() {
        return dato;
    }

}
