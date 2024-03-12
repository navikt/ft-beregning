package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;


import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FordelingDto {

    @Valid
    @JsonProperty(value = "vurderNyttInntektsforholdDto")
    private VurderNyttInntektsforholdDto vurderNyttInntektsforholdDto;

    @Valid
    @JsonProperty(value = "vurderRepresentererStortinget")
    private VurderRepresentererStortingetDto vurderRepresentererStortinget;

    @Valid
    @JsonProperty(value = "fordelBeregningsgrunnlag")
    private FordelBeregningsgrunnlagDto fordelBeregningsgrunnlag;

    public FordelBeregningsgrunnlagDto getFordelBeregningsgrunnlag() {
        return fordelBeregningsgrunnlag;
    }

    public void setFordelBeregningsgrunnlag(FordelBeregningsgrunnlagDto fordelBeregningsgrunnlag) {
        this.fordelBeregningsgrunnlag = fordelBeregningsgrunnlag;
    }

    public VurderNyttInntektsforholdDto getVurderNyttInntektsforholdDto() {
        return vurderNyttInntektsforholdDto;
    }

    public void setVurderNyttInntektsforholdDto(VurderNyttInntektsforholdDto vurderNyttInntektsforholdDto) {
        this.vurderNyttInntektsforholdDto = vurderNyttInntektsforholdDto;
    }

    public VurderRepresentererStortingetDto getVurderRepresentererStortinget() {
        return vurderRepresentererStortinget;
    }

    public void setVurderRepresentererStortinget(VurderRepresentererStortingetDto vurderRepresentererStortinget) {
        this.vurderRepresentererStortinget = vurderRepresentererStortinget;
    }
}
