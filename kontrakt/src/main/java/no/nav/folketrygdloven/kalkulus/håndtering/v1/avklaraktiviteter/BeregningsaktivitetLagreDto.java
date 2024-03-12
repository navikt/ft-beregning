package no.nav.folketrygdloven.kalkulus.håndtering.v1.avklaraktiviteter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class BeregningsaktivitetLagreDto {

    @JsonProperty("opptjeningAktivitetType")
    @Valid
    @NotNull
    private OpptjeningAktivitetType opptjeningAktivitetType;

    @JsonProperty("fom")
    @Valid
    @NotNull
    private LocalDate fom;

    @JsonProperty("tom")
    @Valid
    @NotNull
    private LocalDate tom;

    @JsonProperty("oppdragsgiverOrg")
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String oppdragsgiverOrg;

    @JsonProperty("arbeidsgiverIdentifikator")
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsgiverIdentifikator;

    @JsonProperty("arbeidsforholdRef")
    @Valid
    private UUID arbeidsforholdRef;

    @JsonProperty("skalBrukes")
    @Valid
    @NotNull
    private boolean skalBrukes;

    public BeregningsaktivitetLagreDto() {
    }

    public BeregningsaktivitetLagreDto(@Valid @NotNull OpptjeningAktivitetType opptjeningAktivitetType,
                                       @Valid @NotNull LocalDate fom,
                                       @Valid @NotNull LocalDate tom,
                                       @Valid @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'") String oppdragsgiverOrg,
                                       @Valid @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'") String arbeidsgiverIdentifikator,
                                       @Valid UUID arbeidsforholdRef,
                                       @Valid @NotNull boolean skalBrukes) {
        this.opptjeningAktivitetType = opptjeningAktivitetType;
        this.fom = fom;
        this.tom = tom;
        this.oppdragsgiverOrg = oppdragsgiverOrg;
        this.arbeidsgiverIdentifikator = arbeidsgiverIdentifikator;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.skalBrukes = skalBrukes;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public String getOppdragsgiverOrg() {
        return oppdragsgiverOrg;
    }

    public String getArbeidsgiverIdentifikator() {
        return arbeidsgiverIdentifikator;
    }

    public UUID getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public boolean getSkalBrukes() {
        return skalBrukes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BeregningsaktivitetLagreDto kladd;

        private Builder() {
            kladd = new BeregningsaktivitetLagreDto();
        }

        public Builder medOpptjeningAktivitetType(OpptjeningAktivitetType opptjeningAktivitetType) {
            kladd.opptjeningAktivitetType = opptjeningAktivitetType;
            return this;
        }

        public Builder medFom(LocalDate fom) {
            kladd.fom = fom;
            return this;
        }

        public Builder medTom(LocalDate tom) {
            kladd.tom = tom;
            return this;
        }

        public Builder medOppdragsgiverOrg(String oppdragsgiverOrg) {
            kladd.oppdragsgiverOrg = oppdragsgiverOrg;
            return this;
        }

        public Builder medArbeidsgiverIdentifikator(String arbeidsgiverIdentifikator) {
            kladd.arbeidsgiverIdentifikator = arbeidsgiverIdentifikator;
            return this;
        }

        public Builder medArbeidsforholdRef(UUID arbeidsforholdRef) {
            kladd.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medSkalBrukes(boolean skalBrukes) {
            kladd.skalBrukes = skalBrukes;
            return this;
        }

        public BeregningsaktivitetLagreDto build() {
            return kladd;
        }
    }
}
