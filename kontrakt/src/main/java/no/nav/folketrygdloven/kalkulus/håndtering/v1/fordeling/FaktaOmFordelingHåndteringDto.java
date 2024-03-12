package no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FaktaOmFordelingHåndteringDto extends HåndterBeregningDto {

    @JsonProperty("fordelBeregningsgrunnlagDto")
    @Valid
    @NotNull
    private FordelBeregningsgrunnlagDto fordelBeregningsgrunnlagDto;

    public FaktaOmFordelingHåndteringDto() {
        super(AvklaringsbehovDefinisjon.FORDEL_BG);
    }

    public FaktaOmFordelingHåndteringDto(@Valid @NotNull FordelBeregningsgrunnlagDto fordelBeregningsgrunnlagDto) {
        super(AvklaringsbehovDefinisjon.FORDEL_BG);
        this.fordelBeregningsgrunnlagDto = fordelBeregningsgrunnlagDto;
    }

    public FordelBeregningsgrunnlagDto getFordelBeregningsgrunnlagDto() {
        return fordelBeregningsgrunnlagDto;
    }
}
