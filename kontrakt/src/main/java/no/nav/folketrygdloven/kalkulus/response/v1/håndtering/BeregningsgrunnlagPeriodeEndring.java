package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPeriodeEndring {

    @JsonProperty(value = "beregningsgrunnlagPrStatusOgAndelEndringer")
    @Valid
    @Size(max = 5000)
    private List<BeregningsgrunnlagPrStatusOgAndelEndring> beregningsgrunnlagPrStatusOgAndelEndringer;


    @JsonProperty(value = "nyttInntektsforholdEndringer")
    @Valid
    @Size(max = 100)
    private List<NyttInntektsforholdEndring> nyttInntektsforholdEndringer;


    @JsonProperty(value = "periode")
    @NotNull
    @Valid
    private Periode periode;

    public BeregningsgrunnlagPeriodeEndring() {
        // For json deserialisering
    }

    public BeregningsgrunnlagPeriodeEndring(@Valid List<BeregningsgrunnlagPrStatusOgAndelEndring> beregningsgrunnlagPrStatusOgAndelEndringer,
                                            @Valid List<NyttInntektsforholdEndring> nyttInntektsforholdEndringer,
                                            @NotNull @Valid Periode periode) {
        this.beregningsgrunnlagPrStatusOgAndelEndringer = beregningsgrunnlagPrStatusOgAndelEndringer;
        this.nyttInntektsforholdEndringer = nyttInntektsforholdEndringer;
        this.periode = periode;
    }

    public List<BeregningsgrunnlagPrStatusOgAndelEndring> getBeregningsgrunnlagPrStatusOgAndelEndringer() {
        return beregningsgrunnlagPrStatusOgAndelEndringer;
    }

    public List<NyttInntektsforholdEndring> getNyttInntektsforholdEndringer() {
        return nyttInntektsforholdEndringer;
    }

    public Periode getPeriode() {
        return periode;
    }
}
