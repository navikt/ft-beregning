package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class ErMottattYtelseEndring {

    @JsonProperty(value = "aktivitetStatus")
    @NotNull
    @Valid
    private AktivitetStatus aktivitetStatus;

    @JsonProperty(value = "arbeidsgiver")
    @Valid
    private Aktør arbeidsgiver;

    @JsonProperty(value = "arbeidsforholdRef")
    @Valid
    private UUID arbeidsforholdRef;

    @JsonProperty(value = "erMottattYtelseEndring")
    @NotNull
    @Valid
    private ToggleEndring erMottattYtelseEndring;

    public ErMottattYtelseEndring() {
        // For json deserialisering
    }

    private ErMottattYtelseEndring(@NotNull @Valid AktivitetStatus aktivitetStatus, @NotNull @Valid ToggleEndring erMottattYtelseEndring) {
        this.aktivitetStatus = aktivitetStatus;
        this.erMottattYtelseEndring = erMottattYtelseEndring;
    }

    private ErMottattYtelseEndring(@NotNull @Valid AktivitetStatus aktivitetStatus,
                                  @Valid Aktør arbeidsgiver,
                                  @Valid UUID arbeidsforholdRef,
                                  @NotNull @Valid ToggleEndring erMottattYtelseEndring) {
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.erMottattYtelseEndring = erMottattYtelseEndring;
    }

    public static ErMottattYtelseEndring lagErMottattYtelseEndringForFrilans(ToggleEndring toggleEndring) {
        return new ErMottattYtelseEndring(AktivitetStatus.FRILANSER, toggleEndring);
    }

    public static ErMottattYtelseEndring lagErMottattYtelseEndringForArbeid(ToggleEndring toggleEndring, Aktør arbeidsgiver, UUID arbeidsforholdRef) {
        return new ErMottattYtelseEndring(AktivitetStatus.ARBEIDSTAKER, arbeidsgiver, arbeidsforholdRef, toggleEndring);
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public UUID getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public ToggleEndring getErMottattYtelseEndring() {
        return erMottattYtelseEndring;
    }
}
