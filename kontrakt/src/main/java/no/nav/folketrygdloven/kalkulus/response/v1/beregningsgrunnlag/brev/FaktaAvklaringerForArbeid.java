package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FaktaAvklaringerForArbeid {

	@JsonProperty(value = "arbeidsgiver")
	@NotNull
	@Valid
	private Arbeidsgiver arbeidsgiver;

	@JsonProperty(value = "arbeidsforholdRef")
	@Valid
	private UUID arbeidsforholdRef;

	@JsonProperty(value = "erTidsbegrenset")
	@Valid
	private Boolean erTidsbegrenset;

	@JsonProperty(value = "harMottattYtelseForArbeidsforhold")
	@Valid
	private Boolean harMottattYtelseForArbeidsforhold;

	@JsonProperty(value = "harLønnsendringIBeregningsperioden")
	@Valid
	private Boolean harLønnsendringIBeregningsperioden;

	protected FaktaAvklaringerForArbeid() {
	}

	public FaktaAvklaringerForArbeid(@NotNull @Valid Arbeidsgiver arbeidsgiver,
	                                 @Valid UUID arbeidsforholdRef,
	                                 @Valid Boolean erTidsbegrenset,
	                                 @Valid Boolean harMottattYtelseForArbeidsforhold,
	                                 @Valid Boolean harLønnsendringIBeregningsperioden) {
		this.arbeidsgiver = arbeidsgiver;
		this.arbeidsforholdRef = arbeidsforholdRef;
		this.erTidsbegrenset = erTidsbegrenset;
		this.harMottattYtelseForArbeidsforhold = harMottattYtelseForArbeidsforhold;
		this.harLønnsendringIBeregningsperioden = harLønnsendringIBeregningsperioden;
	}

	public Arbeidsgiver getArbeidsgiver() {
		return arbeidsgiver;
	}

	public UUID getArbeidsforholdRef() {
		return arbeidsforholdRef;
	}

	public Boolean getErTidsbegrenset() {
		return erTidsbegrenset;
	}


	public Boolean getHarMottattYtelseForArbeidsforhold() {
		return harMottattYtelseForArbeidsforhold;
	}

	public Boolean getHarLønnsendringIBeregningsperioden() {
		return harLønnsendringIBeregningsperioden;
	}
}
