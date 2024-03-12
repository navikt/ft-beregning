package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class Sammenligningsgrunnlag {

    @JsonProperty(value = "sammenligningsperiode")
    @NotNull
    @Valid
    private Periode sammenligningsperiode;

    @JsonProperty(value = "rapportertPrÅr")
    @NotNull
    @Valid
    private Beløp rapportertPrÅr;

    @JsonProperty(value = "avvikPromilleNy")
    @NotNull
    @Valid
    private BigDecimal avvikPromilleNy;

    public Sammenligningsgrunnlag() {
    }

    public Sammenligningsgrunnlag(@NotNull @Valid Periode sammenligningsperiode, @NotNull @Valid Beløp rapportertPrÅr, @NotNull @Valid BigDecimal avvikPromilleNy) {
        this.sammenligningsperiode = sammenligningsperiode;
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

}
