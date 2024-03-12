package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPeriodeDto {

    @Valid
    @JsonProperty("beregningsgrunnlagPeriodeFom")
    private LocalDate beregningsgrunnlagPeriodeFom;

    @Valid
    @JsonProperty("beregningsgrunnlagPeriodeTom")
    private LocalDate beregningsgrunnlagPeriodeTom;

    @Valid
    @JsonProperty("beregnetPrAar")
    private Beløp beregnetPrAar;

    @Valid
    @JsonProperty("bruttoPrAar")
    private Beløp bruttoPrAar;

    @Valid
    @JsonProperty("bruttoInkludertBortfaltNaturalytelsePrAar")
    private Beløp bruttoInkludertBortfaltNaturalytelsePrAar;

    @Valid
    @JsonProperty("avkortetPrAar")
    private Beløp avkortetPrAar;

    @Valid
    @JsonProperty("redusertPrAar")
    private Beløp redusertPrAar;

    @Valid
    @Size()
    @JsonProperty("periodeAarsaker")
    private Set<PeriodeÅrsak> periodeAarsaker = new HashSet<>();

    @Valid
    @JsonProperty("dagsats")
    @Digits(integer = 8, fraction = 2)
    @DecimalMin("0.00")
    @DecimalMax("10000000.00")
    private Long dagsats;

    @Valid
    @Size()
    @JsonProperty("beregningsgrunnlagPrStatusOgAndel")
    private List<BeregningsgrunnlagPrStatusOgAndelDto> beregningsgrunnlagPrStatusOgAndel;

    public BeregningsgrunnlagPeriodeDto() {
        // trengs for deserialisering av JSON
    }

    public LocalDate getBeregningsgrunnlagPeriodeFom() {
        return beregningsgrunnlagPeriodeFom;
    }

    public LocalDate getBeregningsgrunnlagPeriodeTom() {
        return beregningsgrunnlagPeriodeTom;
    }

    public Beløp getBeregnetPrAar() {
        return beregnetPrAar;
    }

    public Beløp getBruttoPrAar() {
        return bruttoPrAar;
    }

    public Beløp getBruttoInkludertBortfaltNaturalytelsePrAar() {
        return bruttoInkludertBortfaltNaturalytelsePrAar;
    }

    public Beløp getAvkortetPrAar() {
        return avkortetPrAar;
    }

    public Beløp getRedusertPrAar() {
        return redusertPrAar;
    }

    public Long getDagsats() {
        return dagsats;
    }

    public List<BeregningsgrunnlagPrStatusOgAndelDto> getBeregningsgrunnlagPrStatusOgAndel() {
        return beregningsgrunnlagPrStatusOgAndel;
    }

    public void setBeregningsgrunnlagPeriodeFom(LocalDate beregningsgrunnlagPeriodeFom) {
        this.beregningsgrunnlagPeriodeFom = beregningsgrunnlagPeriodeFom;
    }

    public void setBeregningsgrunnlagPeriodeTom(LocalDate beregningsgrunnlagPeriodeTom) {
        this.beregningsgrunnlagPeriodeTom = beregningsgrunnlagPeriodeTom;
    }

    public void setBeregnetPrAar(Beløp beregnetPrAar) {
        this.beregnetPrAar = beregnetPrAar;
    }

    public void setBruttoPrAar(Beløp bruttoPrAar) {
        this.bruttoPrAar = bruttoPrAar;
    }

    public void setBruttoInkludertBortfaltNaturalytelsePrAar(Beløp bruttoInkludertBortfaltNaturalytelsePrAar) {
        this.bruttoInkludertBortfaltNaturalytelsePrAar = bruttoInkludertBortfaltNaturalytelsePrAar;
    }

    public void setAvkortetPrAar(Beløp avkortetPrAar) {
        this.avkortetPrAar = avkortetPrAar;
    }

    public void setRedusertPrAar(Beløp redusertPrAar) {
        this.redusertPrAar = redusertPrAar;
    }

    public void setAndeler(List<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        this.beregningsgrunnlagPrStatusOgAndel = andeler;
    }

    public void setDagsats(Long dagsats) {
        this.dagsats = dagsats;
    }

    void leggTilPeriodeAarsak(PeriodeÅrsak periodeAarsak) {
        periodeAarsaker.add(periodeAarsak);
    }

    public void leggTilPeriodeAarsaker(List<PeriodeÅrsak> periodeAarsaker) {
        for (PeriodeÅrsak aarsak : periodeAarsaker) {
            leggTilPeriodeAarsak(aarsak);
        }
    }

    public void setPeriodeAarsaker(Set<PeriodeÅrsak> periodeAarsaker) {
        this.periodeAarsaker = periodeAarsaker;
    }

    public Set<PeriodeÅrsak> getPeriodeAarsaker() {
        return periodeAarsaker;
    }

}
