package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningAktiviteterEndring {

    @JsonProperty(value = "aktivitetEndringer")
    @NotNull
    @Size()
    @Valid
    private List<BeregningAktivitetEndring> aktivitetEndringer;

    public BeregningAktiviteterEndring() {
    }

    public BeregningAktiviteterEndring(List<BeregningAktivitetEndring> aktivitetEndringer) {
        this.aktivitetEndringer = aktivitetEndringer;
    }

    public List<BeregningAktivitetEndring> getAktivitetEndringer() {
        return aktivitetEndringer;
    }
}
