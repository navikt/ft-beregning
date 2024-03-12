package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.BeregningsgrunnlagPrReferanse;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagListe {

    @JsonProperty(value = "beregningsgrunnlagListe", required = true)
    @Valid
    @NotNull
    private List<BeregningsgrunnlagPrReferanse<BeregningsgrunnlagDto>> beregningsgrunnlagListe;

    @JsonProperty(value = "trengerNyInput")
    @Valid
    private boolean trengerNyInput;

    public BeregningsgrunnlagListe() {
        // JSON deserialisering
    }

    public BeregningsgrunnlagListe(@JsonProperty(value = "beregningsgrunnlagListe", required = true) @Valid @NotNull List<BeregningsgrunnlagPrReferanse<BeregningsgrunnlagDto>> beregningsgrunnlagListe) {
        this.beregningsgrunnlagListe = beregningsgrunnlagListe;
    }

    public BeregningsgrunnlagListe(@Valid Boolean trengerNyInput) {
        this.trengerNyInput = trengerNyInput;
    }

    public List<BeregningsgrunnlagPrReferanse<BeregningsgrunnlagDto>> getBeregningsgrunnlagListe() {
        return beregningsgrunnlagListe;
    }

    public Boolean getTrengerNyInput() {
        return trengerNyInput;
    }
}
