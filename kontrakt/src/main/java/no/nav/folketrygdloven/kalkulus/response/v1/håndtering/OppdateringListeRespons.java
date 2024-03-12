package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Beskriver hvilke endringer som er gjort på beregningsgrunnlaget ved løst avklaringsbehov
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class OppdateringListeRespons {

    @JsonProperty(value = "oppdateringer")
    @Valid
    private List<OppdateringPrRequest> oppdateringer;

    @JsonProperty(value = "trengerNyInput")
    @Valid
    private Boolean trengerNyInput;


    public OppdateringListeRespons() {
    }

    public OppdateringListeRespons(@Valid Boolean trengerNyInput) {
        this.trengerNyInput = trengerNyInput;
    }

    public OppdateringListeRespons(@Valid @NotNull List<OppdateringPrRequest> oppdateringer) {
        this.oppdateringer = oppdateringer;
    }

    public List<OppdateringPrRequest> getOppdateringer() {
        return oppdateringer;
    }

    public boolean trengerNyInput() {
        return trengerNyInput != null && trengerNyInput;
    }

}
