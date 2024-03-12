package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class InntektskategoriEndring {

    @JsonProperty(value = "fraVerdi")
    @Valid
    private Inntektskategori fraVerdi;

    @JsonProperty(value = "tilVerdi")
    @NotNull
    @Valid
    private Inntektskategori tilVerdi;

    public InntektskategoriEndring() {
        // For Json deserialisering
    }

    public InntektskategoriEndring(@Valid Inntektskategori fraVerdi, @NotNull @Valid Inntektskategori tilVerdi) {
        this.fraVerdi = fraVerdi;
        this.tilVerdi = tilVerdi;
    }

    public Inntektskategori getFraVerdi() {
        return fraVerdi;
    }

    public Inntektskategori getTilVerdi() {
        return tilVerdi;
    }

}
