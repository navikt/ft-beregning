package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

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
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningAktivitetDto {

    /** For virksomheter - orgnr. For personlige arbeidsgiver - aktørId. */
    @Valid
    @JsonProperty(value = "arbeidsgiverIdent")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsgiverIdent;

    @Valid
    @JsonProperty(value = "eksternArbeidsforholdId")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String eksternArbeidsforholdId;

    @Valid
    @JsonProperty(value = "fom")
    @NotNull
    private LocalDate fom;

    @Valid
    @JsonProperty(value = "tom")
    @NotNull
    private LocalDate tom;

    @Valid
    @JsonProperty(value = "arbeidsforholdId")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsforholdId;

    @Valid
    @JsonProperty(value = "arbeidsforholdType")
    @NotNull
    private OpptjeningAktivitetType arbeidsforholdType;

    @Valid
    @JsonProperty(value = "skalBrukes")
    private Boolean skalBrukes;

    public BeregningAktivitetDto() {
        // jackson
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public void setArbeidsforholdId(String arbeidsforholdId) {
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public String getArbeidsgiverIdent() {
        return arbeidsgiverIdent;
    }

    public void setArbeidsgiverIdent(String arbeidsgiverIdent) {
        this.arbeidsgiverIdent = arbeidsgiverIdent;
    }

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public OpptjeningAktivitetType getArbeidsforholdType() {
        return arbeidsforholdType;
    }

    public void setArbeidsforholdType(OpptjeningAktivitetType arbeidsforholdType) {
        this.arbeidsforholdType = arbeidsforholdType;
    }

    public Boolean getSkalBrukes() {
        return skalBrukes;
    }

    public void setSkalBrukes(Boolean skalBrukes) {
        this.skalBrukes = skalBrukes;
    }

    public String getEksternArbeidsforholdId() {
        return eksternArbeidsforholdId;
    }

    public void setEksternArbeidsforholdId(String eksternArbeidsforholdId) {
        this.eksternArbeidsforholdId = eksternArbeidsforholdId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeregningAktivitetDto that = (BeregningAktivitetDto) o;
        return Objects.equals(arbeidsgiverIdent, that.arbeidsgiverIdent) &&
            Objects.equals(fom, that.fom) &&
            Objects.equals(tom, that.tom) &&
            Objects.equals(arbeidsforholdId, that.arbeidsforholdId) &&
            Objects.equals(eksternArbeidsforholdId, that.eksternArbeidsforholdId) &&
            Objects.equals(arbeidsforholdType, that.arbeidsforholdType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiverIdent, fom, tom, arbeidsforholdId, eksternArbeidsforholdId, arbeidsforholdType);
    }
}
