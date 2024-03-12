package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fp;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class BesteberegningInntektDto {

    @Valid
    @JsonProperty(value = "arbeidsgiverId")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsgiverId;

    @Valid
    @JsonProperty(value = "arbeidsgiverIdent")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsgiverIdent;

    @Valid
    @JsonProperty(value = "arbeidsforholdId")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsforholdId;

    @Valid
    @JsonProperty(value = "opptjeningAktivitetType")
    @NotNull
    private OpptjeningAktivitetType opptjeningAktivitetType;

    @Valid
    @JsonProperty(value = "inntekt")
    @NotNull
    private final Beløp inntekt;

    public BesteberegningInntektDto(OpptjeningAktivitetType opptjeningAktivitetType, Beløp inntekt) {
        this.opptjeningAktivitetType = opptjeningAktivitetType;
        this.inntekt = inntekt;
    }

    public BesteberegningInntektDto(String arbeidsgiverId, String arbeidsgiverIdent, String arbeidsforholdId, Beløp inntekt) {
        this.arbeidsgiverId = arbeidsgiverId;
        this.arbeidsforholdId = arbeidsforholdId;
        this.inntekt = inntekt;
        this.arbeidsgiverIdent = arbeidsgiverIdent;
    }

    public String getArbeidsgiverId() {
        return arbeidsgiverId;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }

    public Beløp getInntekt() {
        return inntekt;
    }

    public String getArbeidsgiverIdent() {
        return arbeidsgiverIdent;
    }
}
