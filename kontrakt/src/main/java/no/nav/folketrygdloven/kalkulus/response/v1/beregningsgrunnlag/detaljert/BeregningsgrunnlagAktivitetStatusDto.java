package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagAktivitetStatusDto {

    @JsonProperty(value = "aktivitetStatus")
    @NotNull
    @Valid
    private AktivitetStatus aktivitetStatus;

    @JsonProperty(value = "hjemmel")
    @NotNull
    @Valid
    private Hjemmel hjemmel;

	public BeregningsgrunnlagAktivitetStatusDto() {
	}

	public BeregningsgrunnlagAktivitetStatusDto(AktivitetStatus aktivitetStatus, Hjemmel hjemmel) {
		this.aktivitetStatus = aktivitetStatus;
		this.hjemmel = hjemmel;
	}

	public AktivitetStatus getAktivitetStatus() {
		return aktivitetStatus;
	}

	public Hjemmel getHjemmel() {
		return hjemmel;
	}
}
