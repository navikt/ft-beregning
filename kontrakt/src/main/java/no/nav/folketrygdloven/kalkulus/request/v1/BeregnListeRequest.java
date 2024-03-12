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
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
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
public class BeregnListeRequest implements KalkulusRequest {

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Valid
    private Saksnummer saksnummer;

    @JsonProperty(value = "behandlingUuid", required = true)
    @Valid
    private UUID behandlingUuid;

    @JsonProperty(value = "aktør", required = true)
    @NotNull
    @Valid
    private PersonIdent aktør;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private FagsakYtelseType ytelseSomSkalBeregnes;

    @JsonProperty(value = "stegType", required = true)
    @NotNull
    @Valid
    private BeregningSteg stegType;

    @JsonProperty(value = "beregnForListe", required = true)
    @Size(min = 1)
    @Valid
    @NotNull
    private List<BeregnForRequest> beregnForListe;

    protected BeregnListeRequest() {
    }

    @JsonCreator
    public BeregnListeRequest(@JsonProperty(value = "saksnummer", required = true) Saksnummer saksnummer,
                              @JsonProperty(value = "behandlingUuid") UUID behandlingUuid,
                              @JsonProperty(value = "aktør", required = true) PersonIdent aktør,
                              @JsonProperty(value = "ytelseSomSkalBeregnes", required = true) FagsakYtelseType ytelseSomSkalBeregnes,
                              @JsonProperty(value = "stegType", required = true) BeregningSteg stegType,
                              @JsonProperty(value = "beregnForListe") List<BeregnForRequest> beregnForListe) {
        this.saksnummer = saksnummer;
        this.behandlingUuid = behandlingUuid;
        this.aktør = aktør;
        this.ytelseSomSkalBeregnes = ytelseSomSkalBeregnes;
        this.stegType = stegType;
        this.beregnForListe = beregnForListe;
    }

    @Override
    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    @Override
    public UUID getBehandlingUuid() {
        return behandlingUuid;
    }

    public PersonIdent getAktør() {
        return aktør;
    }

    public FagsakYtelseType getYtelseSomSkalBeregnes() {
        return ytelseSomSkalBeregnes;
    }

    public BeregningSteg getStegType() {
        return stegType;
    }

    public List<BeregnForRequest> getBeregnForListe() {
        return beregnForListe;
    }

    @AssertTrue
    public boolean isSkalHaInputForFørsteSteg() {
        return stegType != BeregningSteg.FASTSETT_STP_BER || beregnForListe.stream().noneMatch(r -> r.getKalkulatorInput() == null);
    }


}
