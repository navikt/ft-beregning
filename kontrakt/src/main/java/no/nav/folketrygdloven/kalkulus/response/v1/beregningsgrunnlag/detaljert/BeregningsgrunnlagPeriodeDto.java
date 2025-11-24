package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPeriodeDto {

    @JsonProperty(value = "beregningsgrunnlagPrStatusOgAndelList")
    @Size(min = 1)
    private List<@Valid BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList;

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
    private List<@Valid PeriodeÅrsak> periodeÅrsaker;


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
     * utbetalingsgrad etter eventuell reduksjon ved tilkommet inntekt. Dersom totalUtbetalingsgradFraUttak er lavere enn inntektsgradering vil denne vere lik totalUtbetalingsgradFraUttak .
     */
    @JsonProperty(value = "totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 4)
    private BigDecimal totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;

    /**
     * Gradering mot bortfalt inntekt (andel bortfalt inntekt utgjør av totalt beregningsgrunnlag)
     */
    @JsonProperty(value = "inntektsgradering")
    @Valid
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 4)
    private BigDecimal inntektsgradering;

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

    public BigDecimal getInntektsgradering() {
        return inntektsgradering;
    }

    public static class Builder {
        private BeregningsgrunnlagPeriodeDto kladd = new BeregningsgrunnlagPeriodeDto();

        public static Builder ny() {
            return new Builder();
        }

        public Builder medBeregningsgrunnlagPrStatusOgAndelList(List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndelList) {
            kladd.beregningsgrunnlagPrStatusOgAndelList = beregningsgrunnlagPrStatusOgAndelList;
            return this;
        }

        public Builder medPeriode(Periode periode) {
            kladd.periode = periode;
            return this;
        }

        public Builder medBruttoPrÅr(Beløp bruttoPrÅr) {
            kladd.bruttoPrÅr = bruttoPrÅr;
            return this;
        }

        public Builder medAvkortetPrÅr(Beløp avkortetPrÅr) {
            kladd.avkortetPrÅr = avkortetPrÅr;
            return this;
        }

        public Builder medRedusertPrÅr(Beløp redusertPrÅr) {
            kladd.redusertPrÅr = redusertPrÅr;
            return this;
        }

        public Builder medDagsats(Long dagsats) {
            kladd.dagsats = dagsats;
            return this;
        }

        public Builder medPeriodeÅrsaker(List<PeriodeÅrsak> periodeÅrsaker) {
            kladd.periodeÅrsaker = periodeÅrsaker;
            return this;
        }

        public Builder medTotalUtbetalingsgradFraUttak(BigDecimal totalUtbetalingsgradFraUttak) {
            kladd.totalUtbetalingsgradFraUttak = totalUtbetalingsgradFraUttak;
            return this;
        }

        public Builder medTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt(BigDecimal totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt) {
            kladd.totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt = totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt;
            return this;
        }

        public Builder medInntektsgradering(BigDecimal inntektsgradering) {
            kladd.inntektsgradering = inntektsgradering;
            return this;
        }

        public Builder medReduksjonsfaktorInaktivTypeA(BigDecimal reduksjonsfaktorInaktivTypeA) {
            kladd.reduksjonsfaktorInaktivTypeA = reduksjonsfaktorInaktivTypeA;
            return this;
        }

        public BeregningsgrunnlagPeriodeDto build() {
            return kladd;
        }

    }

}
