package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektAktivitetType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class InntektsgrunnlagInntektDto {

    @Valid
    @NotNull
    @JsonProperty(value = "inntektAktivitetType")
    private InntektAktivitetType inntektAktivitetType;

    @Valid
    @JsonProperty("beløp")
    private Beløp beløp;

	@Valid
	@JsonProperty("arbeidsgiverIdent")
	@Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message="'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
	private String arbeidsgiverIdent;

	public InntektsgrunnlagInntektDto() {
		// For jackson
	}

    public InntektsgrunnlagInntektDto(@Valid @NotNull InntektAktivitetType inntektAktivitetType,
                                      @Valid Beløp beløp,
                                      @Valid String arbeidsgiverIdent) {
        this.inntektAktivitetType = inntektAktivitetType;
        this.beløp = beløp;
	    this.arbeidsgiverIdent = arbeidsgiverIdent;
    }

    public Beløp getBeløp() {
        return beløp;
    }

    public InntektAktivitetType getInntektAktivitetType() {
        return inntektAktivitetType;
    }

	public String getArbeidsgiverIdent() {
		return arbeidsgiverIdent;
	}
}
