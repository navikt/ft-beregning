package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class NyttInntektsforholdEndring {

    @JsonProperty(value = "aktivitetStatus")
    @NotNull
    @Valid
    private AktivitetStatus aktivitetStatus;

    @JsonProperty(value = "arbeidsgiver")
    @Valid
    private Aktør arbeidsgiver;

    @JsonProperty(value = "bruttoInntektPrÅrEndring")
    @Valid
    @NotNull
    private InntektEndring bruttoInntektPrÅrEndring;

    @JsonProperty(value = "skalRedusereUtbetalingEndring")
    @Valid
    @NotNull
    private ToggleEndring skalRedusereUtbetalingEndring;

    public NyttInntektsforholdEndring() {
    }

    public NyttInntektsforholdEndring(AktivitetStatus aktivitetStatus, Aktør arbeidsgiver, InntektEndring bruttoInntektPrÅrEndring, ToggleEndring skalRedusereUtbetalingEndring) {
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiver = arbeidsgiver;
        this.bruttoInntektPrÅrEndring = bruttoInntektPrÅrEndring;
        this.skalRedusereUtbetalingEndring = skalRedusereUtbetalingEndring;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InntektEndring getBruttoInntektPrÅrEndring() {
        return bruttoInntektPrÅrEndring;
    }

    public ToggleEndring getSkalRedusereUtbetalingEndring() {
        return skalRedusereUtbetalingEndring;
    }
}
