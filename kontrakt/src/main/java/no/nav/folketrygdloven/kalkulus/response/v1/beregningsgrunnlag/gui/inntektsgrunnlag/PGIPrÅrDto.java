package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class PGIPrÅrDto {

    @Valid
    @NotNull
    @JsonProperty(value = "år")
    @Min(1900)
    @Max(3000)
    private Integer år;

    @Valid
    @JsonProperty(value = "inntekter")
    @Size(max = 3)
    private List<PGIGrunnlagDto> inntekter;

    public PGIPrÅrDto() {
    }

    public PGIPrÅrDto(@Valid @NotNull @Min(1900) @Max(3000) Integer år,
                      @Valid @NotNull @Min(0) @Max(2) List<PGIGrunnlagDto> inntekter) {
        this.år = år;
        this.inntekter = inntekter;
    }

    public Integer getÅr() {
        return år;
    }

    public List<PGIGrunnlagDto> getInntekter() {
        return inntekter;
    }
}
