package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

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
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPeriodeDto {

    @JsonProperty(value = "beregningsgrunnlagPrStatusOgAndelList")
    @Size(min = 1)
    @Valid
    private List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList;

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

    @JsonProperty(value = "dagsats")
    @Valid
    @Min(0)
    @Max(178956970)
    private Long dagsats;

    @JsonProperty(value = "periodeÅrsaker")
    @Size(min = 1)
    @Valid
    private List<PeriodeÅrsak> periodeÅrsaker;


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

    /**
     * Reduksjonsfaktor benyttet ved midlertidig inaktiv type A (§8-47a)
     */
    @JsonProperty(value = "reduksjonsfaktorInaktivTypeA")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 4)
    private BigDecimal reduksjonsfaktorInaktivTypeA;


    public BeregningsgrunnlagPeriodeDto() {
    }

    public BeregningsgrunnlagPeriodeDto(@NotNull @Valid List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList,
                                        @NotNull @Valid Periode periode,
                                        @Valid Beløp bruttoPrÅr,
                                        @Valid Beløp avkortetPrÅr,
                                        @Valid Beløp redusertPrÅr,
                                        @Valid Long dagsats,
                                        @NotNull @Valid List<PeriodeÅrsak> periodeÅrsaker,
                                        BigDecimal totalUtbetalingsgradFraUttak,
                                        BigDecimal totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt,
                                        BigDecimal reduksjonsfaktorInaktivTypeA) {
        this.beregningsgrunnlagPrStatusOgAndelList = beregningsgrunnlagPrStatusOgAndelList;
        this.periode = periode;
        this.bruttoPrÅr = bruttoPrÅr;
        this.avkortetPrÅr = avkortetPrÅr;
        this.redusertPrÅr = redusertPrÅr;
        this.dagsats = dagsats;
        this.periodeÅrsaker = periodeÅrsaker;
        this.totalUtbetalingsgradFraUttak = totalUtbetalingsgradFraUttak;
        this.totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt = totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;
        this.reduksjonsfaktorInaktivTypeA = reduksjonsfaktorInaktivTypeA;
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

    public Long getDagsats() {
        return dagsats;
    }

    public List<PeriodeÅrsak> getPeriodeÅrsaker() {
        return periodeÅrsaker;
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

    public BigDecimal getReduksjonsfaktorInaktivTypeA() {
        return reduksjonsfaktorInaktivTypeA;
    }
}
