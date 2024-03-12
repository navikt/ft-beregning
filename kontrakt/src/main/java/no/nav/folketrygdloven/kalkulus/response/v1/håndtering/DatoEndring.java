package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class DatoEndring {

    @JsonProperty(value = "fraVerdi")
    @Valid
    private LocalDate fraVerdi;

    @JsonProperty(value = "tilVerdi")
    @NotNull
    @Valid
    private LocalDate tilVerdi;

    public DatoEndring() {
        // For Json deserialisering
    }

    public DatoEndring(@Valid LocalDate fraVerdi, @Valid @NotNull LocalDate tilVerdi) {
        this.fraVerdi = fraVerdi;
        this.tilVerdi = tilVerdi;
    }

    public LocalDate getFraVerdi() {
        return fraVerdi;
    }

    public LocalDate getTilVerdi() {
        return tilVerdi;
    }

}
