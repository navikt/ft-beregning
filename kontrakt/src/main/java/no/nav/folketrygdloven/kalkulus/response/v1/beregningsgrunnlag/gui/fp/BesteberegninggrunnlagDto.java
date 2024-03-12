package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fp;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class BesteberegninggrunnlagDto {

    @Valid
    @JsonProperty("besteMåneder")
    @Size(min = 6, max = 6)
    private final List<BesteberegningMånedGrunnlagDto> besteMåneder;

    @Valid
    @JsonProperty("avvik")
    private Beløp avvik;

    public BesteberegninggrunnlagDto(List<BesteberegningMånedGrunnlagDto> besteMåneder, Beløp avvik) {
        this.besteMåneder = besteMåneder;
        this.avvik = avvik;
    }

    public List<BesteberegningMånedGrunnlagDto> getBesteMåneder() {
        return besteMåneder;
    }

    public Beløp getAvvik() {
        return avvik;
    }
}

