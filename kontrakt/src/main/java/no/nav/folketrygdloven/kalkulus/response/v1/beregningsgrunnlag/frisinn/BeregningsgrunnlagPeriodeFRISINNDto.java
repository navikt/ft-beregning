package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPeriodeFRISINNDto {

    @JsonProperty(value = "beregningsgrunnlagPrStatusOgAndelList")
    @Size(min = 1)
    @Valid
    private List<BeregningsgrunnlagPrStatusOgAndelFRISINNDto> beregningsgrunnlagPrStatusOgAndelList;

    @JsonProperty(value = "periode")
    @NotNull
    @Valid
    private Periode periode;

    @JsonProperty(value = "bruttoPrÅr")
    @Valid
    private Beløp bruttoPrÅr;

    @JsonProperty(value = "avkortetPrÅr")
    @Valid
    private Beløp avkortetPrÅr;

    @JsonProperty(value = "redusertPrÅr")
    @Valid
    private Beløp redusertPrÅr;

    @JsonProperty(value = "bgFratrukketInntektstak")
    @NotNull
    @Valid
    private Beløp bgFratrukketInntektstak;

    @JsonProperty(value = "dagsats")
    @Valid
    @Min(0)
    @Max(178956970)
    private Long dagsats;

    @JsonProperty(value = "periodeÅrsaker")
    @Size(min = 1)
    @Valid
    private List<PeriodeÅrsak> periodeÅrsaker;

    public BeregningsgrunnlagPeriodeFRISINNDto() {
    }

    public BeregningsgrunnlagPeriodeFRISINNDto(@JsonProperty(value = "beregningsgrunnlagPrStatusOgAndelList") @NotNull @Valid List<BeregningsgrunnlagPrStatusOgAndelFRISINNDto> beregningsgrunnlagPrStatusOgAndelList,
                                               @JsonProperty(value = "periode") @NotNull @Valid Periode periode,
                                               @JsonProperty(value = "bruttoPrÅr") @Valid Beløp bruttoPrÅr,
                                               @JsonProperty(value = "avkortetPrÅr") @Valid Beløp avkortetPrÅr,
                                               @JsonProperty(value = "redusertPrÅr") @Valid Beløp redusertPrÅr,
                                               @JsonProperty(value = "bgFratrukketInntektstak") @Valid Beløp bgFratrukketInntektstak,
                                               @JsonProperty(value = "dagsats") @Valid Long dagsats,
                                               @JsonProperty(value = "periodeÅrsaker") @NotNull @Valid List<PeriodeÅrsak> periodeÅrsaker) {

        this.beregningsgrunnlagPrStatusOgAndelList = beregningsgrunnlagPrStatusOgAndelList;
        this.periode = periode;
        this.bruttoPrÅr = bruttoPrÅr;
        this.avkortetPrÅr = avkortetPrÅr;
        this.redusertPrÅr = redusertPrÅr;
        this.bgFratrukketInntektstak = bgFratrukketInntektstak;
        this.dagsats = dagsats;
        this.periodeÅrsaker = periodeÅrsaker;
    }

    public LocalDate getBeregningsgrunnlagPeriodeFom() {
        return periode.getFom();
    }

    public LocalDate getBeregningsgrunnlagPeriodeTom() {
        return periode.getTom();
    }

    public Beløp getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public Beløp getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public Beløp getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public Beløp getBgFratrukketInntektstak() {
        return bgFratrukketInntektstak;
    }

    public Long getDagsats() {
        return dagsats;
    }

    public List<PeriodeÅrsak> getPeriodeÅrsaker() {
        return periodeÅrsaker;
    }

    public List<BeregningsgrunnlagPrStatusOgAndelFRISINNDto> getBeregningsgrunnlagPrStatusOgAndelList() {
        return beregningsgrunnlagPrStatusOgAndelList;
    }

    public Periode getPeriode() {
        return periode;
    }
}
