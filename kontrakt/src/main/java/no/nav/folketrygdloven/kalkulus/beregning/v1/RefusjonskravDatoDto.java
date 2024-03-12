package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class RefusjonskravDatoDto {

    @JsonProperty(value = "arbeidsgiver")
    @Valid
    @NotNull
    private Aktør arbeidsgiver;

    @JsonProperty(value = "førsteDagMedRefusjonskrav")
    @Valid
    @NotNull
    private LocalDate førsteDagMedRefusjonskrav;

    @JsonProperty(value = "førsteInnsendingAvRefusjonskrav")
    @Valid
    @NotNull
    private LocalDate førsteInnsendingAvRefusjonskrav;

    @JsonProperty(value = "harRefusjonFraStart")
    @Valid
    @NotNull
    private Boolean harRefusjonFraStart;

    public RefusjonskravDatoDto(@Valid @NotNull Aktør arbeidsgiver,
                                @Valid @NotNull LocalDate førsteDagMedRefusjonskrav,
                                @Valid @NotNull LocalDate førsteInnsendingAvRefusjonskrav,
                                @Valid @NotNull Boolean harRefusjonFraStart) {

        this.arbeidsgiver = arbeidsgiver;
        this.førsteDagMedRefusjonskrav = førsteDagMedRefusjonskrav;
        this.førsteInnsendingAvRefusjonskrav = førsteInnsendingAvRefusjonskrav;
        this.harRefusjonFraStart = harRefusjonFraStart;
    }

    protected RefusjonskravDatoDto() {
        // jackson
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public LocalDate getFørsteDagMedRefusjonskrav() {
        return førsteDagMedRefusjonskrav;
    }

    public LocalDate getFørsteInnsendingAvRefusjonskrav() {
        return førsteInnsendingAvRefusjonskrav;
    }

    public boolean harRefusjonFraStart() {
        return harRefusjonFraStart;
    }

    @Override
    public String toString() {
        return "RefusjonskravDatoDto{" +
                "arbeidsgiver=" + arbeidsgiver +
                ", førsteDagMedRefusjonskrav=" + førsteDagMedRefusjonskrav +
                ", førsteInnsendingAvRefusjonskrav=" + førsteInnsendingAvRefusjonskrav +
                ", harRefusjonFraStart=" + harRefusjonFraStart +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefusjonskravDatoDto that = (RefusjonskravDatoDto) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
                Objects.equals(førsteDagMedRefusjonskrav, that.førsteDagMedRefusjonskrav) &&
                Objects.equals(førsteInnsendingAvRefusjonskrav, that.førsteInnsendingAvRefusjonskrav) &&
                Objects.equals(harRefusjonFraStart, that.harRefusjonFraStart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, førsteDagMedRefusjonskrav, førsteInnsendingAvRefusjonskrav, harRefusjonFraStart);
    }
}
