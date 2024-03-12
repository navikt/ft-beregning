package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningAktivitetDto {

    @JsonProperty(value = "periode")
    @NotNull
    @Valid
    private Periode periode;

    @JsonProperty(value = "arbeidsgiver")
    @Valid
    private Arbeidsgiver arbeidsgiver;

    @JsonProperty(value = "arbeidsforholdRef")
    @Valid
    private InternArbeidsforholdRefDto arbeidsforholdRef;

    @JsonProperty(value = "opptjeningAktivitetType")
    @NotNull
    @Valid
    private OpptjeningAktivitetType opptjeningAktivitetType;

    public BeregningAktivitetDto() {
    }

    public BeregningAktivitetDto(@NotNull @Valid Periode periode, @Valid Arbeidsgiver arbeidsgiver, @Valid InternArbeidsforholdRefDto arbeidsforholdRef, @NotNull @Valid OpptjeningAktivitetType opptjeningAktivitetType) {
        this.periode = periode;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.opptjeningAktivitetType = opptjeningAktivitetType;
    }

    public Periode getPeriode() {
        return periode;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }
}
