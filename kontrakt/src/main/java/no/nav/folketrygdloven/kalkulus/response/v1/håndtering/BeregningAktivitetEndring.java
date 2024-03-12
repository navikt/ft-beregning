package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningAktivitetEndring {

    @JsonProperty(value = "aktivitetNøkkel")
    @NotNull
    @Valid
    private BeregningAktivitetNøkkel aktivitetNøkkel;

    @JsonProperty(value = "skalBrukesEndring")
    @Valid
    private ToggleEndring skalBrukesEndring;

    @JsonProperty(value = "tomDatoEndring")
    @Valid
    private DatoEndring tomDatoEndring;

    public BeregningAktivitetEndring() {
    }

    public BeregningAktivitetEndring(BeregningAktivitetNøkkel aktivitetNøkkel,
                                     ToggleEndring skalBrukesEndring,
                                     DatoEndring tomDatoEndring) {
        this.aktivitetNøkkel = aktivitetNøkkel;
        this.skalBrukesEndring = skalBrukesEndring;
        this.tomDatoEndring = tomDatoEndring;
    }

    public BeregningAktivitetNøkkel getAktivitetNøkkel() {
        return aktivitetNøkkel;
    }

    public ToggleEndring getSkalBrukesEndring() {
        return skalBrukesEndring;
    }

    public DatoEndring getTomDatoEndring() {
        return tomDatoEndring;
    }
}
