package no.nav.folketrygdloven.kalkulus.request.v1;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;

/**
 * Spesifikasjon for å oppdatere grunnlaget med informasjon fra saksbehandler.
 *
 * Må minimum angi en referanser kobling
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
public class HåndterBeregningRequest {

    @JsonProperty(value = "håndterBeregning")
    @NotNull
    @Valid
    private HåndterBeregningDto håndterBeregning;

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;


    public HåndterBeregningRequest(@NotNull @Valid HåndterBeregningDto håndterBeregning,
                                   @Valid @NotNull UUID eksternReferanse) {
        this.håndterBeregning = håndterBeregning;
        this.eksternReferanse = eksternReferanse;
    }

    public HåndterBeregningRequest() {
        // jackson
    }

    public HåndterBeregningDto getHåndterBeregning() {
        return håndterBeregning;
    }

    public UUID getEksternReferanse() {
        return eksternReferanse;
    }
}
