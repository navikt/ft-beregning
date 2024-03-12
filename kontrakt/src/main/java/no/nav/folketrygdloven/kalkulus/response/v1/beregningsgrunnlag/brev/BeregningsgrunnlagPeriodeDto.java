package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPeriodeDto {

    @JsonProperty(value = "beregningsgrunnlagPrStatusOgAndelList")
    @Size(min = 1)
    @Valid
    private List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList;

    @JsonProperty(value = "tilkommetInntektsforholdList")
    @Size(max = 100)
    @Valid
    private List<TilkommetInntektsforholdDto> tilkommetInntektsforholdList;

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

    @JsonProperty(value = "dagsats")
    @Valid
    @Min(0)
    @Max(178956970)
    private Long dagsats;

    /**
     * utbetalingsgrad dersom det kun skulle vært gradert mot uttak
     */
    @JsonProperty(value = "totalUtbetalingsgradFraUttak")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 4)
    private BigDecimal totalUtbetalingsgradFraUttak;

    /**
     * utbetalingsgrad dersom det kun skulle vært gradert mot tilkommetInntekt
     */
    @JsonProperty(value = "totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 4)
    private BigDecimal totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;


    public BeregningsgrunnlagPeriodeDto() {
    }

    public BeregningsgrunnlagPeriodeDto(@NotNull @Valid List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList,
                                        List<TilkommetInntektsforholdDto> tilkommetInntektsforholdList, @NotNull @Valid Periode periode,
                                        @Valid Beløp bruttoPrÅr,
                                        @Valid Beløp avkortetPrÅr,
                                        @Valid Long dagsats,
                                        BigDecimal totalUtbetalingsgradFraUttak,
                                        BigDecimal totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt) {
        this.beregningsgrunnlagPrStatusOgAndelList = beregningsgrunnlagPrStatusOgAndelList;
        this.tilkommetInntektsforholdList = tilkommetInntektsforholdList;
        this.periode = periode;
        this.bruttoPrÅr = bruttoPrÅr;
        this.avkortetPrÅr = avkortetPrÅr;
        this.dagsats = dagsats;
        this.totalUtbetalingsgradFraUttak = totalUtbetalingsgradFraUttak;
        this.totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt = totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;
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

    public Long getDagsats() {
        return dagsats;
    }

    public List<BeregningsgrunnlagPrStatusOgAndelDto> getBeregningsgrunnlagPrStatusOgAndelList() {
        return beregningsgrunnlagPrStatusOgAndelList;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getTotalUtbetalingsgradFraUttak() {
        return totalUtbetalingsgradFraUttak;
    }

    public BigDecimal getTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt() {
        return totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;
    }

    public List<TilkommetInntektsforholdDto> getTilkommetInntektsforholdList() {
        return tilkommetInntektsforholdList;
    }
}
