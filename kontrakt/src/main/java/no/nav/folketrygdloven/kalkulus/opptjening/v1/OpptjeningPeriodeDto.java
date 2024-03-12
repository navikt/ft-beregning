package no.nav.folketrygdloven.kalkulus.opptjening.v1;

import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OpptjeningPeriodeDto {


    @JsonProperty(value = "opptjeningAktivitetType", required = true)
    @Valid
    @NotNull
    private OpptjeningAktivitetType opptjeningAktivitetType;

    @JsonProperty(value = "periode", required = true)
    @Valid
    @NotNull
    private Periode periode;

    @JsonProperty(value = "arbeidsgiver")
    @Valid
    private Aktør arbeidsgiver;

    @JsonProperty(value = "abakusReferanse")
    @Valid
    private InternArbeidsforholdRefDto abakusReferanse;

    public OpptjeningPeriodeDto() {
        // For Json deserialisering
    }

    public OpptjeningPeriodeDto(@Valid @NotNull OpptjeningAktivitetType opptjeningAktivitetType, @Valid @NotNull Periode periode) {
        this.opptjeningAktivitetType = opptjeningAktivitetType;
        this.periode = periode;
    }

    public OpptjeningPeriodeDto(@JsonProperty(value = "opptjeningAktivitetType", required = true) @Valid @NotNull OpptjeningAktivitetType opptjeningAktivitetType,
                                @JsonProperty(value = "periode", required = true) @Valid @NotNull Periode periode,
                                @JsonProperty(value = "arbeidsgiver") @Valid Aktør arbeidsgiver,
                                @JsonProperty(value = "abakusReferanse") @Valid InternArbeidsforholdRefDto abakusReferanse) {

        this.opptjeningAktivitetType = opptjeningAktivitetType;
        this.periode = periode;
        this.arbeidsgiver = arbeidsgiver;
        this.abakusReferanse = abakusReferanse;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }

    public Periode getPeriode() {
        return periode;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getAbakusReferanse() {
        return abakusReferanse;
    }

    @Override
    public int hashCode() {
        return Objects.hash(opptjeningAktivitetType, periode, arbeidsgiver, abakusReferanse);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpptjeningPeriodeDto that = (OpptjeningPeriodeDto) o;
        return opptjeningAktivitetType.equals(that.opptjeningAktivitetType) &&
                periode.equals(that.periode) &&
                arbeidsgiver.equals(that.arbeidsgiver) &&
                abakusReferanse.equals(that.abakusReferanse);
    }
}
