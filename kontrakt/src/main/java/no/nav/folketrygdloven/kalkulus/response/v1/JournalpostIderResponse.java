package no.nav.folketrygdloven.kalkulus.response.v1;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.JournalpostId;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class JournalpostIderResponse {


	@JsonProperty(value = "eksternReferanse")
	@Valid
	private UUID eksternReferanse;

	@JsonProperty(value = "journalpostider")
	@Valid
	private List<JournalpostId> journalpostIder;

	public JournalpostIderResponse() {
	}

	public JournalpostIderResponse(@JsonProperty(value = "eksternReferanse") UUID eksternReferanse, @JsonProperty(value = "journalpostider") List<JournalpostId> journalpostIder) {
		this.eksternReferanse = eksternReferanse;
		this.journalpostIder = journalpostIder;
	}

	public UUID getEksternReferanse() {
		return eksternReferanse;
	}

	public List<JournalpostId> getJournalpostIder() {
		return journalpostIder;
	}
}
