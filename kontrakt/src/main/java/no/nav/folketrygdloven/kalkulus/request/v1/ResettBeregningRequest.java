package no.nav.folketrygdloven.kalkulus.request.v1;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Spesifikasjon for å fortsette en beregning.
 * <p>
 * Må minimum angi en referanser kobling
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class ResettBeregningRequest {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;

    @JsonProperty(value = "behandlingAvsluttetTid", required = true)
    @Valid
    @NotNull
    private LocalDateTime behandlingAvsluttetTid;

    protected ResettBeregningRequest() {
    }

    @JsonCreator
    public ResettBeregningRequest(UUID eksternReferanse,
                                  LocalDateTime behandlingAvsluttetTid) {
        this.eksternReferanse = eksternReferanse;
        this.behandlingAvsluttetTid = behandlingAvsluttetTid;
    }

    public UUID getEksternReferanse() {
        return eksternReferanse;
    }

    public LocalDateTime getBehandlingAvsluttetTid() {
        return behandlingAvsluttetTid;
    }

}
