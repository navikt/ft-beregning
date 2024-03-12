package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;


import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FordelBeregningsgrunnlagPeriodeDto {

    @Valid
    @JsonProperty(value = "fom")
    @NotNull
    private LocalDate fom;

    @Valid
    @JsonProperty(value = "tom")
    private LocalDate tom;

    @Valid
    @JsonProperty(value = "fordelBeregningsgrunnlagAndeler")
    @Size(min = 1, max = 100)
    @NotNull
    private List<FordelBeregningsgrunnlagAndelDto> fordelBeregningsgrunnlagAndeler = new ArrayList<>();

    @Valid
    @JsonProperty(value = "harPeriodeAarsakGraderingEllerRefusjon")
    private boolean harPeriodeAarsakGraderingEllerRefusjon;

    @Valid
    @JsonProperty(value = "skalRedigereInntekt")
    private boolean skalRedigereInntekt;

    @Valid
    @JsonProperty(value = "skalPreutfyllesMedBeregningsgrunnlag")
    private boolean skalPreutfyllesMedBeregningsgrunnlag;

    @Valid
    @JsonProperty(value = "skalKunneEndreRefusjon")
    private boolean skalKunneEndreRefusjon;


    public boolean isHarPeriodeAarsakGraderingEllerRefusjon() {
        return harPeriodeAarsakGraderingEllerRefusjon;
    }

    public void setHarPeriodeAarsakGraderingEllerRefusjon(boolean harPeriodeAarsakGraderingEllerRefusjon) {
        this.harPeriodeAarsakGraderingEllerRefusjon = harPeriodeAarsakGraderingEllerRefusjon;
    }

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    public void setFordelBeregningsgrunnlagAndeler(List<FordelBeregningsgrunnlagAndelDto> fordelBeregningsgrunnlagAndeler) {
        this.fordelBeregningsgrunnlagAndeler = fordelBeregningsgrunnlagAndeler;
    }

    public List<FordelBeregningsgrunnlagAndelDto> getFordelBeregningsgrunnlagAndeler() {
        return fordelBeregningsgrunnlagAndeler;
    }

    public boolean isSkalKunneEndreRefusjon() {
        return skalKunneEndreRefusjon;
    }

    public void setSkalKunneEndreRefusjon(boolean skalKunneEndreRefusjon) {
        this.skalKunneEndreRefusjon = skalKunneEndreRefusjon;
    }

    public boolean isSkalRedigereInntekt() {
        return skalRedigereInntekt;
    }

    public void setSkalRedigereInntekt(boolean skalRedigereInntekt) {
        this.skalRedigereInntekt = skalRedigereInntekt;
    }

    public boolean isSkalPreutfyllesMedBeregningsgrunnlag() {
        return skalPreutfyllesMedBeregningsgrunnlag;
    }

    public void setSkalPreutfyllesMedBeregningsgrunnlag(boolean skalPreutfyllesMedBeregningsgrunnlag) {
        this.skalPreutfyllesMedBeregningsgrunnlag = skalPreutfyllesMedBeregningsgrunnlag;
    }
}
