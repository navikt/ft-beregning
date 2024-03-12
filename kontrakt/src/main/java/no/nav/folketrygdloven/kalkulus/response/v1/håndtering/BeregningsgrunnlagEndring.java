package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import jakarta.validation.Valid;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagEndring {

    @JsonProperty(value = "beregningsgrunnlagPeriodeEndringer")
    @Valid
    private List<BeregningsgrunnlagPeriodeEndring> beregningsgrunnlagPeriodeEndringer;


    public BeregningsgrunnlagEndring() {
    }

    public BeregningsgrunnlagEndring(@Valid List<BeregningsgrunnlagPeriodeEndring> beregningsgrunnlagPeriodeEndringer) {
        this.beregningsgrunnlagPeriodeEndringer = beregningsgrunnlagPeriodeEndringer;
    }

    public List<BeregningsgrunnlagPeriodeEndring> getBeregningsgrunnlagPeriodeEndringer() {
        return beregningsgrunnlagPeriodeEndringer;
    }
}
