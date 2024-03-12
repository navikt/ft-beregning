package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

public class KontrollerInputForSakerRequest {

    @JsonProperty(value = "saksnummer", required = true)
    @Valid
    @NotNull
    private List<Saksnummer> saksnummer;


    protected KontrollerInputForSakerRequest() {
        // default ctor
    }

    public KontrollerInputForSakerRequest(@Valid @NotNull List<Saksnummer> saksnummer) {
        this.saksnummer = saksnummer;
    }

    public List<Saksnummer> getSaksnummer() {
        return saksnummer;
    }

}
