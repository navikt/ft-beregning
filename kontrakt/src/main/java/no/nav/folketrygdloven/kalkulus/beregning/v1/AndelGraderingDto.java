package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.List;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AndelGraderingDto {

    @JsonProperty(value = "aktivitetStatus")
    @Valid
    @NotNull
    private AktivitetStatus aktivitetStatus;

    @JsonProperty(value = "arbeidsgiver")
    @Valid
    private Aktør arbeidsgiver;

    @JsonProperty(value = "arbeidsforholdRef")
    @Valid
    private InternArbeidsforholdRefDto arbeidsforholdRef;

    @JsonProperty(value = "graderinger")
    @Valid
    @Size(min = 1)
    private List<GraderingDto> graderinger;

    protected AndelGraderingDto() {
        // default ctor
    }

    public AndelGraderingDto(@Valid @NotNull AktivitetStatus aktivitetStatus,
                             @Valid @NotNull Aktør arbeidsgiver,
                             @Valid InternArbeidsforholdRefDto arbeidsforholdRef,
                             @Valid @NotEmpty List<GraderingDto> graderinger) {

        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.graderinger = graderinger;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    @JsonProperty(value = "graderinger")
    public List<GraderingDto> getGraderinger() {
        return graderinger;
    }

    @Override
    public String toString() {
        return "AndelGraderingDto{" +
                "aktivitetStatus=" + aktivitetStatus +
                ", arbeidsgiver=" + arbeidsgiver +
                ", arbeidsforholdRef=" + arbeidsforholdRef +
                ", graderinger=" + graderinger +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AndelGraderingDto that = (AndelGraderingDto) o;
        return Objects.equals(aktivitetStatus, that.aktivitetStatus) &&
                Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
                Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef) &&
                Objects.equals(graderinger, that.graderinger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus, arbeidsgiver, arbeidsforholdRef, graderinger);
    }
}
