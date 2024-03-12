package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

/**
 * Spesifikasjon for å hente aktivt beregningsgrunnlag.
 * Henter aktivt beregningsgrunnlag
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class BeregningsgrunnlagListeRequest implements KalkulusRequest {

    @JsonProperty(value = "beregningsgrunnlagRequest", required = true)
    @Valid
    @NotNull
    private List<BeregningsgrunnlagRequest> beregningsgrunnlagRequest;

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Valid
    private Saksnummer saksnummer;

    @JsonProperty(value = "behandlingUuid", required = true)
    @Valid
    private UUID behandlingUuid;

    protected BeregningsgrunnlagListeRequest() {
    }

    @JsonCreator
    public BeregningsgrunnlagListeRequest(@JsonProperty(value = "saksnummer", required = true) Saksnummer saksnummer,
                                          @JsonProperty(value = "beregningsgrunnlagRequest", required = true) List<BeregningsgrunnlagRequest> requestPrReferanse,
                                          @JsonProperty(value = "behandlingUuid") UUID behandlingUuid) {
        this.beregningsgrunnlagRequest = Objects.requireNonNull(requestPrReferanse, "requestPrReferanse");
        this.saksnummer = Objects.requireNonNull(saksnummer, "saksnummer");
        this.behandlingUuid = behandlingUuid;
    }

    public List<BeregningsgrunnlagRequest> getRequestPrReferanse() {
        return List.copyOf(beregningsgrunnlagRequest);
    }

    @Override
    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    @Override
    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }
}
