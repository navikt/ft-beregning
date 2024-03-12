package no.nav.folketrygdloven.kalkulus.request.v1;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

public class HentForSakRequest {

    @JsonProperty(value = "saksnummer", required = true)
    @Valid
    @NotNull
    private Saksnummer saksnummer;


    protected HentForSakRequest() {
        // default ctor
    }

    public HentForSakRequest(@Valid @NotNull Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

}
