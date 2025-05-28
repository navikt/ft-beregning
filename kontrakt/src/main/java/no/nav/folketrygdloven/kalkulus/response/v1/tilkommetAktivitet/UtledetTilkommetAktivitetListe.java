package no.nav.folketrygdloven.kalkulus.response.v1.tilkommetAktivitet;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class UtledetTilkommetAktivitetListe {

    @JsonProperty(value = "liste", required = true)
    @Valid
    @NotNull
    private List<UtledetTilkommetAktivitetPrReferanse> liste;

    public UtledetTilkommetAktivitetListe() {
    }

    public UtledetTilkommetAktivitetListe(List<UtledetTilkommetAktivitetPrReferanse> liste) {
        this.liste = liste;
    }

    public List<UtledetTilkommetAktivitetPrReferanse> getListe() {
        return liste;
    }
}
