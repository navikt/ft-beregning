package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Beskriver hvilke endringer som er gjort på beregningsgrunnlaget ved løst avklaringsbehov
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class OppdateringPrRequest {

    @JsonProperty(value = "oppdatering", required = true)
    @Valid
    @NotNull
    private OppdateringRespons oppdatering;

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;

    public OppdateringPrRequest() {
    }

    public OppdateringPrRequest(@Valid @NotNull OppdateringRespons oppdatering, @Valid @NotNull UUID eksternReferanse) {
        this.oppdatering = oppdatering;
        this.eksternReferanse = eksternReferanse;
    }

    public OppdateringRespons getOppdatering() {
        return oppdatering;
    }

    public UUID getEksternReferanse() {
        return eksternReferanse;
    }
}
