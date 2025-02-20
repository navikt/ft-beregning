package no.nav.folketrygdloven.kalkulus.migrering;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagTilstand;

public class BeregningsgrunnlagGrunnlagMigreringDto extends BaseMigreringDto {
    @Valid
    private BeregningsgrunnlagMigreringDto beregningsgrunnlag;

    @Valid
    @NotNull
    private BeregningAktivitetAggregatMigreringDto registerAktiviteter;

    @Valid
    private BeregningAktivitetAggregatMigreringDto saksbehandletAktiviteter;

    @Valid
    private BeregningAktivitetOverstyringerMigreringDto overstyringer;

    @Valid
    private BeregningRefusjonOverstyringerMigreringDto refusjonOverstyringer;

    @Valid
    private FaktaAggregatMigreringDto faktaAggregat;

    @Valid
    @NotNull
    private BeregningsgrunnlagTilstand beregningsgrunnlagTilstand;

    @Valid
    @NotNull
    @Size(max=10)
    private List<RegelSporingGrunnlagMigreringDto> grunnlagsporinger;

    @Valid
    @NotNull
    @Size(max=200)
    private List<RegelSporingPeriodeMigreringDto> periodesporinger;

    BeregningsgrunnlagGrunnlagMigreringDto() {
    }

    public BeregningsgrunnlagGrunnlagMigreringDto(BeregningsgrunnlagMigreringDto beregningsgrunnlag,
                                                  BeregningAktivitetAggregatMigreringDto registerAktiviteter,
                                                  BeregningAktivitetAggregatMigreringDto saksbehandletAktiviteter,
                                                  BeregningAktivitetOverstyringerMigreringDto overstyringer,
                                                  BeregningRefusjonOverstyringerMigreringDto refusjonOverstyringer,
                                                  FaktaAggregatMigreringDto faktaAggregat,
                                                  BeregningsgrunnlagTilstand beregningsgrunnlagTilstand,
                                                  List<RegelSporingGrunnlagMigreringDto> grunnlagsporinger,
                                                  List<RegelSporingPeriodeMigreringDto> periodesporinger) {
        this.beregningsgrunnlag = beregningsgrunnlag;
        this.registerAktiviteter = registerAktiviteter;
        this.saksbehandletAktiviteter = saksbehandletAktiviteter;
        this.overstyringer = overstyringer;
        this.refusjonOverstyringer = refusjonOverstyringer;
        this.faktaAggregat = faktaAggregat;
        this.beregningsgrunnlagTilstand = beregningsgrunnlagTilstand;
        this.grunnlagsporinger = grunnlagsporinger;
        this.periodesporinger = periodesporinger;
    }

    public BeregningsgrunnlagMigreringDto getBeregningsgrunnlag() {
        return beregningsgrunnlag;
    }

    public BeregningAktivitetAggregatMigreringDto getRegisterAktiviteter() {
        return registerAktiviteter;
    }

    public BeregningAktivitetAggregatMigreringDto getSaksbehandletAktiviteter() {
        return saksbehandletAktiviteter;
    }

    public BeregningAktivitetOverstyringerMigreringDto getOverstyringer() {
        return overstyringer;
    }

    public BeregningRefusjonOverstyringerMigreringDto getRefusjonOverstyringer() {
        return refusjonOverstyringer;
    }

    public FaktaAggregatMigreringDto getFaktaAggregat() {
        return faktaAggregat;
    }

    public BeregningsgrunnlagTilstand getBeregningsgrunnlagTilstand() {
        return beregningsgrunnlagTilstand;
    }

    public List<RegelSporingGrunnlagMigreringDto> getGrunnlagsporinger() {
        return grunnlagsporinger;
    }

    public List<RegelSporingPeriodeMigreringDto> getPeriodesporinger() {
        return periodesporinger;
    }
}
