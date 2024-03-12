package no.nav.folketrygdloven.kalkulus.request.v1.simulerTilkommetInntekt;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
public class FinnSimulerTilkommetInntektInputRequest {

    @JsonProperty(value = "saksnummer")
    @Valid
    private Saksnummer saksnummer;


    public FinnSimulerTilkommetInntektInputRequest() {
    }

    @JsonCreator
    public FinnSimulerTilkommetInntektInputRequest(@JsonProperty(value = "saksnummer") Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

}
