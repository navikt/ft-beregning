package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Spesifikasjon for å hente aktivt beregningsgrunnlag.
 * Henter aktivt beregningsgrunnlag
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class BeregningsgrunnlagRequest {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;

    protected BeregningsgrunnlagRequest() {
        // default ctor
    }

    public BeregningsgrunnlagRequest(@Valid @NotNull UUID eksternReferanse) {

        this.eksternReferanse = eksternReferanse;
    }

    public UUID getKoblingReferanse() {
        return eksternReferanse;
    }

}
