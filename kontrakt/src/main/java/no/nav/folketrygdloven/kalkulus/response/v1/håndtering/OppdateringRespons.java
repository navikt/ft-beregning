package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import no.nav.folketrygdloven.kalkulus.response.v1.KalkulusRespons;

/**
 * Beskriver hvilke endringer som er gjort på beregningsgrunnlaget ved løst avklaringsbehov
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class OppdateringRespons implements KalkulusRespons {

    @JsonProperty(value = "eksternReferanse")
    @Valid
    private UUID eksternReferanse;

    @JsonProperty(value = "beregningsgrunnlagEndring")
    @Valid
    private BeregningsgrunnlagEndring beregningsgrunnlagEndring;

    @JsonProperty(value = "beregningAktiviteterEndring")
    @Valid
    private BeregningAktiviteterEndring beregningAktiviteterEndring;

    @JsonProperty(value = "faktaOmBeregningVurderinger")
    @Valid
    private FaktaOmBeregningVurderinger faktaOmBeregningVurderinger;

    @JsonProperty(value = "refusjonoverstyringEndring")
    @Valid
    private RefusjonoverstyringEndring refusjonoverstyringEndring;

    @JsonProperty(value = "varigEndretEllerNyoppstartetNæringEndring")
    @Valid
    private VarigEndretEllerNyoppstartetNæringEndring varigEndretEllerNyoppstartetNæringEndring;

    public OppdateringRespons() {
    }

    public OppdateringRespons(Endringer endringer, UUID eksternReferanse) {
        this.beregningsgrunnlagEndring = endringer.getBeregningsgrunnlagEndring();
        this.beregningAktiviteterEndring = endringer.getBeregningAktiviteterEndring();
        this.faktaOmBeregningVurderinger = endringer.getFaktaOmBeregningVurderinger();
        this.refusjonoverstyringEndring = endringer.getRefusjonoverstyringEndring();
        this.varigEndretEllerNyoppstartetNæringEndring = endringer.getVarigEndretNæringEndring();
        this.eksternReferanse = eksternReferanse;
    }

    public static OppdateringRespons TOM_RESPONS() {
        return new OppdateringRespons();
    }

    public BeregningsgrunnlagEndring getBeregningsgrunnlagEndring() {
        return beregningsgrunnlagEndring;
    }

    public BeregningAktiviteterEndring getBeregningAktiviteterEndring() {
        return beregningAktiviteterEndring;
    }

    public FaktaOmBeregningVurderinger getFaktaOmBeregningVurderinger() {
        return faktaOmBeregningVurderinger;
    }

    public RefusjonoverstyringEndring getRefusjonoverstyringEndring() {
        return refusjonoverstyringEndring;
    }

    public VarigEndretEllerNyoppstartetNæringEndring getVarigEndretNæringEndring() {
        return varigEndretEllerNyoppstartetNæringEndring;
    }

    @Override
    public UUID getEksternReferanse() {
        return eksternReferanse;
    }
}
