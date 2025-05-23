package no.nav.folketrygdloven.kalkulus.migrering;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

public class BeregningsgrunnlagPeriodeMigreringDto extends BaseMigreringDto {

    @Valid
    @Size(max=100)
    private List<BeregningsgrunnlagPrStatusOgAndelMigreringDto> beregningsgrunnlagPrStatusOgAndelList = new ArrayList<>();

    @Valid
    @NotNull
    private Periode periode;

    @Valid
    private Beløp bruttoPrÅr;

    @Valid
    private Beløp avkortetPrÅr;

    @Valid
    private Beløp redusertPrÅr;

    @Valid
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "3000.00")
    @Digits(integer = 4, fraction = 0)
    private Long dagsats;

	@Valid
	@Size(max=10)
    private List<PeriodeÅrsak> beregningsgrunnlagPeriodeÅrsaker = new ArrayList<>();


    public BeregningsgrunnlagPeriodeMigreringDto() {
    }

    public BeregningsgrunnlagPeriodeMigreringDto(List<BeregningsgrunnlagPrStatusOgAndelMigreringDto> beregningsgrunnlagPrStatusOgAndelList,
                                                 Periode periode,
                                                 Beløp bruttoPrÅr,
                                                 Beløp avkortetPrÅr,
                                                 Beløp redusertPrÅr,
                                                 Long dagsats,
                                                 List<PeriodeÅrsak> beregningsgrunnlagPeriodeÅrsaker) {
        this.beregningsgrunnlagPrStatusOgAndelList = beregningsgrunnlagPrStatusOgAndelList;
        this.periode = periode;
        this.bruttoPrÅr = bruttoPrÅr;
        this.avkortetPrÅr = avkortetPrÅr;
        this.redusertPrÅr = redusertPrÅr;
        this.dagsats = dagsats;
        this.beregningsgrunnlagPeriodeÅrsaker = beregningsgrunnlagPeriodeÅrsaker;
    }

    public @Valid List<BeregningsgrunnlagPrStatusOgAndelMigreringDto> getBeregningsgrunnlagPrStatusOgAndelList() {
        return beregningsgrunnlagPrStatusOgAndelList;
    }

    public @Valid @NotNull Periode getPeriode() {
        return periode;
    }

    public @Valid Beløp getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public @Valid Beløp getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public @Valid Beløp getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public @Valid Long getDagsats() {
        return dagsats;
    }

    public List<PeriodeÅrsak> getBeregningsgrunnlagPeriodeÅrsaker() {
        return beregningsgrunnlagPeriodeÅrsaker;
    }
}
