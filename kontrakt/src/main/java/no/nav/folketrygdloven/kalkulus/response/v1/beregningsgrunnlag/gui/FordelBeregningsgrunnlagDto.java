package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FordelBeregningsgrunnlagDto {

    @Valid
    @JsonProperty(value = "fordelBeregningsgrunnlagPerioder")
    @Size(max=5000)
    @NotNull
    private List<FordelBeregningsgrunnlagPeriodeDto> fordelBeregningsgrunnlagPerioder = new ArrayList<>();

    @Valid
    @JsonProperty(value = "arbeidsforholdTilFordeling")
    @Size(max=500)
    @NotNull
    private List<FordelBeregningsgrunnlagArbeidsforholdDto> arbeidsforholdTilFordeling = new ArrayList<>();

    public List<FordelBeregningsgrunnlagPeriodeDto> getFordelBeregningsgrunnlagPerioder() {
        return fordelBeregningsgrunnlagPerioder;
    }

    public void setFordelBeregningsgrunnlagPerioder(List<FordelBeregningsgrunnlagPeriodeDto> fordelBeregningsgrunnlagPerioder) {
        this.fordelBeregningsgrunnlagPerioder = fordelBeregningsgrunnlagPerioder;
    }

    public List<FordelBeregningsgrunnlagArbeidsforholdDto> getArbeidsforholdTilFordeling() {
        return arbeidsforholdTilFordeling;
    }

    public void leggTilArbeidsforholdTilFordeling(FordelBeregningsgrunnlagArbeidsforholdDto arbeidsforholdTilFordeling) {
        this.arbeidsforholdTilFordeling.add(arbeidsforholdTilFordeling);
    }
}
