package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagGrunnlagDto {

    @JsonProperty(value = "beregningsgrunnlag")
    @Valid
    private BeregningsgrunnlagDto beregningsgrunnlag;

    @JsonProperty(value = "faktaAggregat")
    @Valid
    private FaktaAggregatDto faktaAggregat;

    @JsonProperty(value = "registerAktiviteter")
    @NotNull
    @Valid
    private BeregningAktivitetAggregatDto registerAktiviteter;

    @JsonProperty(value = "saksbehandletAktiviteter")
    @Valid
    private BeregningAktivitetAggregatDto saksbehandletAktiviteter;

    @JsonProperty(value = "overstyringer")
    @Valid
    private BeregningAktivitetOverstyringerDto overstyringer;

    @JsonProperty(value = "refusjonOverstyringer")
    @Valid
    private BeregningRefusjonOverstyringerDto refusjonOverstyringer;

    @JsonProperty(value = "beregningsgrunnlagTilstand")
    @NotNull
    @Valid
    private BeregningsgrunnlagTilstand beregningsgrunnlagTilstand;

    public BeregningsgrunnlagGrunnlagDto() {
    }

    public BeregningsgrunnlagGrunnlagDto(@Valid BeregningsgrunnlagDto beregningsgrunnlag,
                                         @Valid FaktaAggregatDto faktaAggregat,
                                         @NotNull @Valid BeregningAktivitetAggregatDto registerAktiviteter,
                                         @Valid BeregningAktivitetAggregatDto saksbehandletAktiviteter,
                                         @Valid BeregningAktivitetOverstyringerDto overstyringer,
                                         @Valid BeregningRefusjonOverstyringerDto refusjonOverstyringer,
                                         @NotNull @Valid BeregningsgrunnlagTilstand beregningsgrunnlagTilstand) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.faktaAggregat = faktaAggregat;
        this.registerAktiviteter = registerAktiviteter;
        this.saksbehandletAktiviteter = saksbehandletAktiviteter;
        this.overstyringer = overstyringer;
        this.refusjonOverstyringer = refusjonOverstyringer;
        this.beregningsgrunnlagTilstand = beregningsgrunnlagTilstand;
    }


    public BeregningsgrunnlagDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public FaktaAggregatDto getFaktaAggregat() {
        return faktaAggregat;
    }

    public BeregningAktivitetAggregatDto getRegisterAktiviteter() {
        return registerAktiviteter;
    }

    public BeregningAktivitetAggregatDto getSaksbehandletAktiviteter() {
        return saksbehandletAktiviteter;
    }

    public BeregningAktivitetOverstyringerDto getOverstyringer() {
        return overstyringer;
    }

    public BeregningRefusjonOverstyringerDto getRefusjonOverstyringer() {
        return refusjonOverstyringer;
    }

    public BeregningsgrunnlagTilstand getBeregningsgrunnlagTilstand() {
        return beregningsgrunnlagTilstand;
    }
}
