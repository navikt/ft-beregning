package no.nav.folketrygdloven.kalkulus.response.v1.gradering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.response.v1.tilkommetAktivitet.UtledetTilkommetAktivitetPrReferanse;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class InntektgraderingListe {

    @JsonProperty(value = "liste", required = true)
    @Valid
    @NotNull
    private List<InntektgraderingPrReferanse> liste;

    public InntektgraderingListe() {
    }

    public InntektgraderingListe(List<InntektgraderingPrReferanse> liste) {
        this.liste = liste;
    }


    public List<InntektgraderingPrReferanse> getListe() {
        return liste;
    }
}
