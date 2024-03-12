package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class RefusjonoverstyringPeriodeEndring {

    @JsonProperty(value = "arbeidsgiver")
    @Valid
    @NotNull
    private Aktør arbeidsgiver;

    @JsonProperty(value = "arbeidsforholdRef")
    @Valid
    private UUID arbeidsforholdRef;

    @JsonProperty(value = "fastsattRefusjonFomEndring")
    @Valid
    @NotNull
    private DatoEndring fastsattRefusjonFomEndring;

    @JsonProperty(value = "fastsattDelvisRefusjonFørDatoEndring")
    @Valid
    private RefusjonEndring fastsattDelvisRefusjonFørDatoEndring;

    public RefusjonoverstyringPeriodeEndring() {
    }

    public RefusjonoverstyringPeriodeEndring(@Valid @NotNull Aktør arbeidsgiver,
                                             @Valid UUID arbeidsforholdRef,
                                             @Valid @NotNull DatoEndring fastsattRefusjonFomEndring,
                                             @Valid RefusjonEndring fastsattDelvisRefusjonFørDatoEndring) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.fastsattRefusjonFomEndring = fastsattRefusjonFomEndring;
        this.fastsattDelvisRefusjonFørDatoEndring = fastsattDelvisRefusjonFørDatoEndring;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public UUID getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public DatoEndring getFastsattRefusjonFomEndring() {
        return fastsattRefusjonFomEndring;
    }

    public RefusjonEndring getFastsattDelvisRefusjonFørDatoEndring() {
        return fastsattDelvisRefusjonFørDatoEndring;
    }
}
