package no.nav.folketrygdloven.kalkulus.request.v1;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

/**
 * Spesifikasjon for å oppdatere grunnlaget med informasjon fra saksbehandler.
 *
 * Må minimum angi en referanser kobling
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
public class HåndterBeregningListeRequest implements KalkulusRequest {

    @JsonProperty(value = "håndterBeregningListe")
    @NotNull
    @Valid
    private List<HåndterBeregningRequest> håndterBeregningListe;

    /** Kalkulatorinput per ekstern kobling referanse. Brukes i tilfelle der input er utdatert */
    @JsonProperty(value = "kalkulatorInput")
    @Valid
    private Map<UUID, KalkulatorInputDto> kalkulatorInputPerKoblingReferanse;

    @JsonProperty(value = "behandlingUuid", required = true)
    @Valid
    @NotNull
    private UUID behandlingUuid;

    @JsonProperty(value = "saksnummer", required = true)
    @Valid
    @NotNull
    private Saksnummer saksnummer;

    // TODO: Sett NotNull og required når k9-sak er oppdatert
    @JsonProperty(value = "ytelseSomSkalBeregnes", required = false)
    @Valid
    private FagsakYtelseType ytelseSomSkalBeregnes;

    @Deprecated
    public HåndterBeregningListeRequest(@NotNull @Valid List<HåndterBeregningRequest> håndterBeregningListe,
                                        @Valid Map<UUID, KalkulatorInputDto> kalkulatorInputPerKoblingReferanse,
                                        @Valid Saksnummer saksnummer,
                                        @Valid @NotNull UUID behandlingUuid) {
        this.håndterBeregningListe = håndterBeregningListe;
        this.kalkulatorInputPerKoblingReferanse = kalkulatorInputPerKoblingReferanse;
        this.saksnummer = saksnummer;
        this.behandlingUuid = behandlingUuid;
    }

    public HåndterBeregningListeRequest(@NotNull @Valid List<HåndterBeregningRequest> håndterBeregningListe,
                                        @Valid Map<UUID, KalkulatorInputDto> kalkulatorInputPerKoblingReferanse,
                                        @Valid FagsakYtelseType ytelseSomSkalBeregnes,
                                        @Valid Saksnummer saksnummer,
                                        @Valid @NotNull UUID behandlingUuid) {
        this.håndterBeregningListe = håndterBeregningListe;
        this.kalkulatorInputPerKoblingReferanse = kalkulatorInputPerKoblingReferanse;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.saksnummer = saksnummer;
        this.behandlingUuid = behandlingUuid;
    }


    @Deprecated
    public HåndterBeregningListeRequest(@NotNull @Valid List<HåndterBeregningRequest> håndterBeregningListe,
                                        @Valid @NotNull UUID behandlingUuid) {
        this.håndterBeregningListe = håndterBeregningListe;
        this.behandlingUuid = behandlingUuid;
    }

    public HåndterBeregningListeRequest(@NotNull @Valid List<HåndterBeregningRequest> håndterBeregningListe,
                                        @Valid FagsakYtelseType ytelseSomSkalBeregnes,
                                        @Valid Saksnummer saksnummer,
                                        @Valid @NotNull UUID behandlingUuid) {
        this.håndterBeregningListe = håndterBeregningListe;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.saksnummer = saksnummer;
        this.behandlingUuid = behandlingUuid;
    }

    public HåndterBeregningListeRequest() {
        // jackson
    }

    public List<HåndterBeregningRequest> getHåndterBeregningListe() {
        return håndterBeregningListe;
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

    public FagsakYtelseType getYtelseSomSkalBeregnes() {
        return ytelseSomSkalBeregnes;
    }
}
