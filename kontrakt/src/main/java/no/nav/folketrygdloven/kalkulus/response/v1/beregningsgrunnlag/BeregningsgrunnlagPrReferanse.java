package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPrReferanse<T> {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;

    @JsonProperty(value = "beregningsgrunnlag")
    @Valid
    private T beregningsgrunnlag;

    @JsonCreator
    public BeregningsgrunnlagPrReferanse(@JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UUID eksternReferanse,
                                         @JsonProperty(value = "beregningsgrunnlag") @Valid T beregningsgrunnlag) {
        this.eksternReferanse = eksternReferanse;
        this.beregningsgrunnlag = beregningsgrunnlag;
    }

    public UUID getEksternReferanse() {
        return eksternReferanse;
    }

    public T getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }
}
