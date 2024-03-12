package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.fp;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.YtelsespesifiktGrunnlagDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ForeldrepengerGrunnlagDto extends YtelsespesifiktGrunnlagDto {

    @Valid
    @JsonProperty("besteberegninggrunnlag")
    private BesteberegninggrunnlagDto besteberegninggrunnlag;

    public ForeldrepengerGrunnlagDto() {
        super();
    }

    public void setBesteberegninggrunnlag(BesteberegninggrunnlagDto besteberegninggrunnlag) {
        this.besteberegninggrunnlag = besteberegninggrunnlag;
    }

    public BesteberegninggrunnlagDto getBesteberegninggrunnlag() {
        return besteberegninggrunnlag;
    }
}

