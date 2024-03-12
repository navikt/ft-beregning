package no.nav.folketrygdloven.kalkulus.opptjening.v1;


import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.JournalpostId;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OppgittOpptjeningDto {

    // Journalpost-id for sporing
    @JsonProperty(value = "journalpostId")
    @Valid
    private JournalpostId journalpostId;

    @JsonProperty(value = "frilans")
    @Valid
    private OppgittFrilansDto frilans;

    @JsonProperty(value = "egenNæring")
    @Valid
    @Size
    private List<OppgittEgenNæringDto> egenNæring;


    @JsonProperty(value = "oppgittArbeidsforhold")
    @Valid
    @Size
    private List<OppgittArbeidsforholdDto> oppgittArbeidsforhold;


    public OppgittOpptjeningDto() {
        // Json deserialisering
    }

    public OppgittOpptjeningDto(JournalpostId journalpostId,
                                OppgittFrilansDto frilans,
                                List<OppgittEgenNæringDto> egenNæring,
                                List<OppgittArbeidsforholdDto> oppgittArbeidsforhold) {
        this.journalpostId = journalpostId;
        this.frilans = frilans;
        this.egenNæring = egenNæring;
        this.oppgittArbeidsforhold = oppgittArbeidsforhold;
    }

    public OppgittFrilansDto getFrilans() {
        return frilans;
    }

    public List<OppgittEgenNæringDto> getEgenNæring() {
        return egenNæring;
    }

    public List<OppgittArbeidsforholdDto> getOppgittArbeidsforhold() {
        return oppgittArbeidsforhold;
    }

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }
}
