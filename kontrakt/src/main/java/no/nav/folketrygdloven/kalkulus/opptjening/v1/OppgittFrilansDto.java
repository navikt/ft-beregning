package no.nav.folketrygdloven.kalkulus.opptjening.v1;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OppgittFrilansDto {

    @JsonProperty("erNyoppstartet")
    @Valid
    @NotNull
    private Boolean erNyoppstartet;

    @JsonProperty("oppgittFrilansInntekt")
    @Valid
    @Size(min = 0)
    private List<OppgittFrilansInntekt> oppgittFrilansInntekt;

    public OppgittFrilansDto() {
        // default ctor
    }

    public OppgittFrilansDto(@Valid @NotNull Boolean erNyoppstartet) {
        this.erNyoppstartet = erNyoppstartet;
    }

    public OppgittFrilansDto(@Valid @NotNull Boolean erNyoppstartet, @Valid @NotNull List<OppgittFrilansInntekt> oppgittFrilansInntekt) {
        this.erNyoppstartet = erNyoppstartet;
        this.oppgittFrilansInntekt = oppgittFrilansInntekt;
    }

    public Boolean getErNyoppstartet() {
        return erNyoppstartet;
    }

    public List<OppgittFrilansInntekt> getOppgittFrilansInntekt() {
        return oppgittFrilansInntekt;
    }
}

