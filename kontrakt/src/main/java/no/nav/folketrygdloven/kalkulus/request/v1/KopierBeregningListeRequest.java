package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.List;
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
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

/**
 * Spesifikasjon for å fortsette en beregning.
 * <p>
 * Må minimum angi en referanser kobling
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class KopierBeregningListeRequest implements KalkulusRequest {

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Valid
    private Saksnummer saksnummer;

    @JsonProperty(value = "behandlingUuid", required = true)
    @Valid
    private UUID behandlingUuid;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private FagsakYtelseType ytelseSomSkalBeregnes;

    /**
     * Definerer steget som det kopieres fra
     */
    @JsonProperty(value = "stegType")
    @Valid
    private BeregningSteg stegType;

    @JsonProperty(value = "fordelBeregningListe", required = true)
    @Size(min=1)
    @NotNull
    @Valid
    private List<KopierBeregningRequest> kopierBeregningListe;


    protected KopierBeregningListeRequest() {
    }

    @JsonCreator
    public KopierBeregningListeRequest(Saksnummer saksnummer,
                                       UUID behandlingUuid,
                                       FagsakYtelseType ytelseSomSkalBeregnes,
                                       BeregningSteg stegType,
                                       List<KopierBeregningRequest> kopierBeregningListe) {
        this.saksnummer = saksnummer;
        this.behandlingUuid = behandlingUuid;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.stegType = stegType;
        this.kopierBeregningListe = kopierBeregningListe;
    }

    @Override
    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    @Override
    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }

    public FagsakYtelseType getYtelseSomSkalBeregnes() {
        return ytelseSomSkalBeregnes;
    }

    public List<KopierBeregningRequest> getKopierBeregningListe() {
        return kopierBeregningListe;
    }

    public BeregningSteg getStegType() {
        return stegType;
    }
}
