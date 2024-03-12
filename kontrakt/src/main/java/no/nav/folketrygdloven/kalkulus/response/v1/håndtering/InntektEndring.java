package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;

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
public class InntektEndring {

    @JsonProperty(value = "fraInntekt")
    @Valid
    private Beløp fraInntekt;

    @JsonProperty(value = "tilInntekt")
    @NotNull
    @Valid
    private Beløp tilInntekt;

    public InntektEndring() {
        // For Json deserialisering
    }

    public InntektEndring(@Valid Beløp fraInntekt, @Valid @NotNull Beløp tilInntekt) {
        this.fraInntekt = fraInntekt;
        this.tilInntekt = tilInntekt;
    }

    public InntektEndring(@Valid BigDecimal fraInntekt, @Valid @NotNull BigDecimal tilInntekt) {
        this.fraInntekt = Beløp.fra(fraInntekt);
        this.tilInntekt = Beløp.fra(tilInntekt);
    }

    public Beløp getFraInntekt() {
        return fraInntekt;
    }

    public Beløp getTilInntekt() {
        return tilInntekt;
    }
}
