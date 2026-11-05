package no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui;

import java.util.List;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.felles.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.JournalpostId;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record RefusjonsandelDto(
        @JsonProperty(value = "arbeidsgiver") @Valid Arbeidsgiver arbeidsgiver,
        @JsonProperty(value = "arbeidsforholdRef") @Valid InternArbeidsforholdRefDto arbeidsforholdRef,
        @JsonProperty(value = "kilde") @Valid RefusjonKilde kilde,
        @JsonProperty(value = "journalpostId") @Valid JournalpostId journalpostId,
        @JsonProperty(value = "refusjonsperioder") List<@Valid RefusjonsperiodeDto> refusjonsperioder) {}
