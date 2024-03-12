package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;


/**
 * Spesifikasjon for å hente beregningsgrunnlagDto for GUI.
 * Henter DTO-struktur som brukes av beregning i frontend
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class HentBeregningsgrunnlagDtoListeForGUIRequest implements KalkulusRequest {

    @JsonProperty(value = "requestPrReferanse", required = true)
    @Valid
    @NotNull
    private List<HentBeregningsgrunnlagDtoForGUIRequest> requestPrReferanse;

    /** Kalkulatorinput per ekstern kobling referanse. Brukes i tilfelle der input er utdatert */
    @JsonProperty(value = "kalkulatorInput")
    @Valid
    private Map<UUID, KalkulatorInputDto> kalkulatorInputPerKoblingReferanse;

    @JsonProperty(value = "behandlingUuid", required = true)
    @Valid
    @NotNull
    private UUID behandlingUuid;

    //TODO: set saksnummer required + @NotNull når fpsak/k9-sak er oppdatert
    @JsonProperty(value = "saksnummer", required = false)
    @Valid
    private Saksnummer saksnummer;

    protected HentBeregningsgrunnlagDtoListeForGUIRequest() {
        // default ctor
    }

    public HentBeregningsgrunnlagDtoListeForGUIRequest(@Valid @NotNull List<HentBeregningsgrunnlagDtoForGUIRequest> requestPrReferanse,
                                                       @Valid Map<UUID, KalkulatorInputDto> kalkulatorInputPerKoblingReferanse,
                                                       @Valid Saksnummer saksnummer,
                                                       @Valid @NotNull UUID behandlingUuid) {
        this.requestPrReferanse = requestPrReferanse;
        this.kalkulatorInputPerKoblingReferanse = kalkulatorInputPerKoblingReferanse;
        this.saksnummer = saksnummer;
        this.behandlingUuid = behandlingUuid;
    }

    public HentBeregningsgrunnlagDtoListeForGUIRequest(@Valid @NotNull List<HentBeregningsgrunnlagDtoForGUIRequest> requestPrReferanse,
                                                       @Valid @NotNull UUID behandlingUuid) {
        this.requestPrReferanse = requestPrReferanse;
        this.behandlingUuid = behandlingUuid;
    }

    public List<HentBeregningsgrunnlagDtoForGUIRequest> getRequestPrReferanse() {
        return requestPrReferanse;
    }

    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }

    public Map<UUID, KalkulatorInputDto> getKalkulatorInputPerKoblingReferanse() {
        return kalkulatorInputPerKoblingReferanse;
    }

    @Override
    public Saksnummer getSaksnummer() {
        return saksnummer;
    }
}
