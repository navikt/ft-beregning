package no.nav.folketrygdloven.kalkulus.request.v1.tilkommetAktivitet;

import java.util.List;

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
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class UtledTilkommetAktivitetListeRequest {

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Valid
    private Saksnummer saksnummer;

    @JsonProperty(value = "ytelseSomSkalBeregnes", required = true)
    @NotNull
    @Valid
    private FagsakYtelseType ytelseType;

    @JsonProperty(value = "liste")
    @Size(min = 1)
    @Valid
    private List<UtledTilkommetAktivitetForRequest> liste;


    protected UtledTilkommetAktivitetListeRequest() {
    }

    @JsonCreator
    public UtledTilkommetAktivitetListeRequest(@JsonProperty(value = "saksnummer", required = true) Saksnummer saksnummer,
                                               @JsonProperty(value = "ytelseType", required = true) FagsakYtelseType ytelseType,
                                               @JsonProperty(value = "liste") List<UtledTilkommetAktivitetForRequest> liste) {
        this.saksnummer = saksnummer;
        this.ytelseType = ytelseType;
        this.liste = liste;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    public FagsakYtelseType getYtelseType() {
        return ytelseType;
    }

    public List<UtledTilkommetAktivitetForRequest> getListe() {
        return liste;
    }
}
