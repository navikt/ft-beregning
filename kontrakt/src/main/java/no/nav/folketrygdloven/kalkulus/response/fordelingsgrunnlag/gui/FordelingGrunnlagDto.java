package no.nav.folketrygdloven.kalkulus.response.fordelingsgrunnlag.gui;

import java.util.List;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record FordelingGrunnlagDto(@NotNull List<@Valid RefusjonsandelDto> refusjonsandeler) {}
