package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class KunYtelseDto {

    @Valid
    @JsonProperty(value = "andeler")
    @Size
    private List<AndelMedBeløpDto> andeler = new ArrayList<>();

    @Valid
    @JsonProperty(value = "fodendeKvinneMedDP")
    private boolean fodendeKvinneMedDP;

    @Valid
    @JsonProperty(value = "erBesteberegning")
    private Boolean erBesteberegning = null;

    public List<AndelMedBeløpDto> getAndeler() {
        return andeler;
    }

    public void setAndeler(List<AndelMedBeløpDto> andeler) {
        this.andeler = andeler;
    }

    public void leggTilAndel(AndelMedBeløpDto andel) {
        andeler.add(andel);
    }

    public boolean isFodendeKvinneMedDP() {
        return fodendeKvinneMedDP;
    }

    public void setFodendeKvinneMedDP(boolean fodendeKvinneMedDP) {
        this.fodendeKvinneMedDP = fodendeKvinneMedDP;
    }

    public Boolean getErBesteberegning() {
        return erBesteberegning;
    }

    public void setErBesteberegning(Boolean erBesteberegning) {
        this.erBesteberegning = erBesteberegning;
    }
}
