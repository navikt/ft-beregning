package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class InntektsgrunnlagDto {

    @NotNull
    @JsonProperty(value = "måneder")
    @Size(max = 12)
    private List<@Valid InntektsgrunnlagMånedDto> måneder;

    @NotNull
    @JsonProperty(value = "pgiGrunnlag")
    @Size(max = 3)
    private List<@Valid PGIPrÅrDto> pgiGrunnlag;

	@NotNull
	@JsonProperty(value = "sammenligningsgrunnlagInntekter")
	@Size(max = 12)
	private List<@Valid InntektsgrunnlagMånedDto> sammenligningsgrunnlagInntekter;

	@NotNull
	@JsonProperty(value = "beregningsgrunnlagInntekter")
	@Size(max = 3)
	private List<@Valid InntektsgrunnlagMånedDto> beregningsgrunnlagInntekter;

    public InntektsgrunnlagDto() {
    }

    public InntektsgrunnlagDto(@NotNull @Min(0) @Max(12) List<@Valid InntektsgrunnlagMånedDto> måneder,
                               @NotNull @Min(0) @Max(3) List<@Valid PGIPrÅrDto> pgiGrunnlag,
                               @NotNull @Min(0) @Max(12) List<@Valid InntektsgrunnlagMånedDto> sammenligningsgrunnlagInntekter,
                               @NotNull @Min(0) @Max(3) List<@Valid InntektsgrunnlagMånedDto> beregningsgrunnlagInntekter) {
        this.måneder = måneder;
        this.pgiGrunnlag = pgiGrunnlag;
		this.sammenligningsgrunnlagInntekter = sammenligningsgrunnlagInntekter;
		this.beregningsgrunnlagInntekter = beregningsgrunnlagInntekter;
    }

    public List<InntektsgrunnlagMånedDto> getMåneder() {
        return måneder;
    }

    public List<PGIPrÅrDto> getPgiGrunnlag() {
        return pgiGrunnlag;
    }

	public List<InntektsgrunnlagMånedDto> getSammenligningsgrunnlagInntekter() {
		return sammenligningsgrunnlagInntekter;
	}

	public List<InntektsgrunnlagMånedDto> getBeregningsgrunnlagInntekter() {
		return beregningsgrunnlagInntekter;
	}
}
