package no.nav.folketrygdloven.kalkulus.response.v1;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.GrunnbeløpReguleringStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class GrunnbeløpReguleringRespons {

    @JsonProperty(value = "resultat")
    @NotNull
    private Map<UUID, @Valid GrunnbeløpReguleringStatus> resultat;

    public GrunnbeløpReguleringRespons() {
    }

    public GrunnbeløpReguleringRespons(@NotNull Map<UUID, @Valid GrunnbeløpReguleringStatus> resultat) {
        this.resultat = resultat;
    }

    public Map<UUID, GrunnbeløpReguleringStatus> getResultat() {
        return resultat;
    }
}
