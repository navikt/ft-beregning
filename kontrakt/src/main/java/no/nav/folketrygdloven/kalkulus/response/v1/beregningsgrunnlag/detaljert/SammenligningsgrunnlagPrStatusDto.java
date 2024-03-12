package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class SammenligningsgrunnlagPrStatusDto {

    @JsonProperty(value = "sammenligningsperiode")
    @NotNull
    @Valid
    private Periode sammenligningsperiode;

    @JsonProperty(value = "sammenligningsgrunnlagType")
    @NotNull
    @Valid
    private SammenligningsgrunnlagType sammenligningsgrunnlagType;

    @JsonProperty(value = "rapportertPrÅr")
    @NotNull
    @Valid
    private Beløp rapportertPrÅr;

    @JsonProperty(value = "avvikPromilleNy")
    @DecimalMin(value = "0.00", message = "verdien ${validatedValue} må være >= {value}")
    @DecimalMax(value = "1000000000.00", message = "verdien ${validatedValue} må være <= {value}")
    @Digits(integer = 10, fraction = 2)
    @NotNull
    @Valid
    private BigDecimal avvikPromilleNy;

    public SammenligningsgrunnlagPrStatusDto() {
    }

    public SammenligningsgrunnlagPrStatusDto(@NotNull @Valid Periode sammenligningsperiode, @NotNull @Valid SammenligningsgrunnlagType sammenligningsgrunnlagType, @NotNull @Valid Beløp rapportertPrÅr, @NotNull @Valid BigDecimal avvikPromilleNy) {
        this.sammenligningsperiode = sammenligningsperiode;
        this.sammenligningsgrunnlagType = sammenligningsgrunnlagType;
        this.rapportertPrÅr = rapportertPrÅr;
        this.avvikPromilleNy = avvikPromilleNy;
    }

    public LocalDate getSammenligningsperiodeFom() {
        return sammenligningsperiode.getFom();
    }

    public LocalDate getSammenligningsperiodeTom() {
        return sammenligningsperiode.getTom();
    }

    public Beløp getRapportertPrÅr() {
        return rapportertPrÅr;
    }

    public BigDecimal getAvvikPromilleNy() {
        return avvikPromilleNy;
    }

    public SammenligningsgrunnlagType getSammenligningsgrunnlagType() {
        return sammenligningsgrunnlagType;
    }

}
