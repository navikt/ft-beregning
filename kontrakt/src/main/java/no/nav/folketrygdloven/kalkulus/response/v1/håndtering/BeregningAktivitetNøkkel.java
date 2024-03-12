package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningAktivitetNøkkel {

    @JsonProperty(value = "opptjeningAktivitetType")
    @NotNull
    @Valid
    private OpptjeningAktivitetType opptjeningAktivitetType;

    @JsonProperty(value = "fom")
    @NotNull
    @Valid
    private LocalDate fom;

    @JsonProperty(value = "arbeidsgiver")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    @Valid
    private Aktør arbeidsgiver;

    @JsonProperty(value = "arbeidsforholdRef")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    @Valid
    private InternArbeidsforholdRefDto arbeidsforholdRef;

    private BeregningAktivitetNøkkel() {
    }

    public BeregningAktivitetNøkkel(OpptjeningAktivitetType opptjeningAktivitetType,
                                    LocalDate fom,
                                    Aktør arbeidsgiver,
                                    InternArbeidsforholdRefDto arbeidsforholdRef) {
        this.opptjeningAktivitetType = opptjeningAktivitetType;
        this.fom = fom;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }

    public LocalDate getFom() {
        return fom;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BeregningAktivitetNøkkel)) {
            return false;
        }
        BeregningAktivitetNøkkel that = (BeregningAktivitetNøkkel) o;
        return Objects.equals(opptjeningAktivitetType, that.opptjeningAktivitetType)
                && Objects.equals(fom, that.fom)
                && Objects.equals(arbeidsgiver, that.arbeidsgiver)
                && Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opptjeningAktivitetType, fom, arbeidsgiver, arbeidsforholdRef);
    }

}
