package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

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
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.SammenligningsgrunnlagType;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class SammenligningsgrunnlagDto {

    @Valid
    @JsonProperty(value = "sammenligningsgrunnlagFom")
    private LocalDate sammenligningsgrunnlagFom;

    @Valid
    @JsonProperty(value = "sammenligningsgrunnlagTom")
    private LocalDate sammenligningsgrunnlagTom;

    @Valid
    @JsonProperty(value = "rapportertPrAar")
    private Beløp rapportertPrAar;

    @Valid
    @JsonProperty(value = "avvikPromille")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal avvikPromille;

    @Valid
    @JsonProperty(value = "avvikProsent")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private BigDecimal avvikProsent;

    @Valid
    @JsonProperty(value = "sammenligningsgrunnlagType")
    private SammenligningsgrunnlagType sammenligningsgrunnlagType;

    @Valid
    @JsonProperty(value = "differanseBeregnet")
    private Beløp differanseBeregnet;

    public SammenligningsgrunnlagDto() {
        // trengs for deserialisering av JSON
    }

    public LocalDate getSammenligningsgrunnlagFom() {
        return sammenligningsgrunnlagFom;
    }

    public LocalDate getSammenligningsgrunnlagTom() {
        return sammenligningsgrunnlagTom;
    }

    public Beløp getRapportertPrAar() {
        return rapportertPrAar;
    }

    public BigDecimal getAvvikPromille() {
        return avvikPromille;
    }

    public SammenligningsgrunnlagType getSammenligningsgrunnlagType() {
        return sammenligningsgrunnlagType;
    }

    public Beløp getDifferanseBeregnet() {
        return differanseBeregnet;
    }

    public void setSammenligningsgrunnlagFom(LocalDate sammenligningsgrunnlagFom) {
        this.sammenligningsgrunnlagFom = sammenligningsgrunnlagFom;
    }

    public void setSammenligningsgrunnlagTom(LocalDate sammenligningsgrunnlagTom) {
        this.sammenligningsgrunnlagTom = sammenligningsgrunnlagTom;
    }

    public void setRapportertPrAar(Beløp rapportertPrAar) {
        this.rapportertPrAar = rapportertPrAar;
    }

    public void setAvvikPromille(BigDecimal avvikPromille) {
        this.avvikPromille = avvikPromille;
    }

    public BigDecimal getAvvikProsent() {
        return avvikProsent;
    }

    public void setAvvikProsent(BigDecimal avvikProsent) {
        this.avvikProsent = avvikProsent;
    }

    public void setSammenligningsgrunnlagType(SammenligningsgrunnlagType sammenligningsgrunnlagType) {
        this.sammenligningsgrunnlagType = sammenligningsgrunnlagType;
    }

    public void setDifferanseBeregnet(Beløp differanseBeregnet) {
        this.differanseBeregnet = differanseBeregnet;
    }
}
