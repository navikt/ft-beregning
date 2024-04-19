package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
public class HentJournalpostIderRequest {

    @JsonProperty(value = "koblinger", required = true)
    @Valid
    @NotNull
    @Size(min=1)
    private List<UUID> koblinger;


    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Valid
    private Saksnummer saksnummer;


    protected HentJournalpostIderRequest() {
        // default ctor
    }

    public HentJournalpostIderRequest(@Valid @NotNull @Size(min = 1) List<UUID> koblinger,
                                      @JsonProperty(value = "saksnummer", required = true) @NotNull @Valid Saksnummer saksnummer) {
        this.koblinger = koblinger;
        this.saksnummer = saksnummer;
    }

    public List<UUID> getKoblinger() {
        return koblinger;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }
}
