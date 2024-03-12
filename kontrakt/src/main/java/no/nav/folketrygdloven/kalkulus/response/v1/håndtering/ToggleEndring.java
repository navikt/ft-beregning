package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class ToggleEndring {

    @JsonProperty(value = "fraVerdi")
    @Valid
    private Boolean fraVerdi;

    @JsonProperty(value = "tilVerdi")
    @NotNull
    @Valid
    private Boolean tilVerdi;

    public ToggleEndring() {
        // For Json deserialisering
    }

    public ToggleEndring(@Valid Boolean fraVerdi, @Valid @NotNull Boolean tilVerdi) {
        this.fraVerdi = fraVerdi;
        this.tilVerdi = tilVerdi;
    }

    public Boolean getFraVerdi() {
        return fraVerdi;
    }

    public Boolean getTilVerdi() {
        return tilVerdi;
    }
}
