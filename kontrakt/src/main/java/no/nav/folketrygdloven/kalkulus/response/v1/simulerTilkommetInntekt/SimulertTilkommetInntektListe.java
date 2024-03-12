package no.nav.folketrygdloven.kalkulus.response.v1.simulerTilkommetInntekt;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.BeregningsgrunnlagPrReferanse;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev.BeregningsgrunnlagDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class SimulertTilkommetInntektListe {

    @JsonProperty(value = "simulertListe", required = true)
    @Valid
    @NotNull
    private List<SimulertTilkommetInntektPrReferanse> simulertListe;

    public SimulertTilkommetInntektListe() {
    }

    public SimulertTilkommetInntektListe(List<SimulertTilkommetInntektPrReferanse> simulertListe) {
        this.simulertListe = simulertListe;
    }

    public List<SimulertTilkommetInntektPrReferanse> getSimulertListe() {
        return simulertListe;
    }
}
