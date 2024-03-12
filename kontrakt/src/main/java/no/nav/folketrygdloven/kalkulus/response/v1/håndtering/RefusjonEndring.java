package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class RefusjonEndring {

    @JsonProperty(value = "fraRefusjon")
    @Valid
    private Beløp fraRefusjon;

    @JsonProperty(value = "tilRefusjon")
    @NotNull
    @Valid
    private Beløp tilRefusjon;

    public RefusjonEndring() {
        // For Json deserialisering
    }

    public RefusjonEndring(@Valid Beløp fraRefusjon, @Valid @NotNull Beløp tilRefusjon) {
        this.fraRefusjon = fraRefusjon;
        this.tilRefusjon = tilRefusjon;
    }

    public Beløp getFraRefusjon() {
        return fraRefusjon;
    }

    public Beløp getTilRefusjon() {
        return tilRefusjon;
    }
}
