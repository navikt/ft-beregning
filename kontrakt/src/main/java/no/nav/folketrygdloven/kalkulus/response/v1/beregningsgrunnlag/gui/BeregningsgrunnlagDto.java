package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Hjemmel;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.inntektsgrunnlag.InntektsgrunnlagDto;
import no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui.refusjon.RefusjonTilVurderingDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagDto {

    @JsonProperty(value = "avklaringsbehov")
    @Size(max = 10)
    @NotNull
    @Valid
    private List<AvklaringsbehovDto> avklaringsbehov = Collections.emptyList();

    @JsonProperty(value = "skjaeringstidspunktBeregning")
    @NotNull
    @Valid
    private LocalDate skjaeringstidspunktBeregning;

    @JsonProperty(value = "skjæringstidspunkt")
    @NotNull
    @Valid
    private LocalDate skjæringstidspunkt;

    @JsonProperty(value = "aktivitetStatus")
    @Size(min = 1)
    @Valid
    private List<AktivitetStatus> aktivitetStatus;

    @JsonProperty(value = "beregningsgrunnlagPeriode")
    @Size(min = 1)
    @Valid
    private List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPeriode;

    @Deprecated//Fjernes og erstattes av sammenligningsgrunnlagPrStatus
    @JsonProperty(value = "sammenligningsgrunnlag")
    @NotNull
    @Valid
    private SammenligningsgrunnlagDto sammenligningsgrunnlag;

    @JsonProperty(value = "sammenligningsgrunnlagPrStatus")
    @Valid
    @Size
    private List<SammenligningsgrunnlagDto> sammenligningsgrunnlagPrStatus;

    @JsonProperty(value = "halvG")
    @Valid
    private Beløp halvG;

    @JsonProperty(value = "grunnbeløp")
    @Valid
    private Beløp grunnbeløp;

    @JsonProperty(value = "faktaOmBeregning")
    @Valid
    private FaktaOmBeregningDto faktaOmBeregning;

    @JsonProperty(value = "andelerMedGraderingUtenBG")
    @Valid
    @Size
    private List<BeregningsgrunnlagPrStatusOgAndelDto> andelerMedGraderingUtenBG;

    @JsonProperty(value = "hjemmel")
    @Valid
    private Hjemmel hjemmel;

    @JsonProperty(value = "faktaOmFordeling")
    @Valid
    private FordelingDto faktaOmFordeling;

    @JsonProperty(value = "dekningsgrad")
    @Valid
    @Min(0)
    @Max(100)
    private int dekningsgrad;

    @JsonProperty(value = "ytelsesspesifiktGrunnlag")
    @Valid
    private YtelsespesifiktGrunnlagDto ytelsesspesifiktGrunnlag;

    @JsonProperty(value = "refusjonTilVurdering")
    @Valid
    private RefusjonTilVurderingDto refusjonTilVurdering;

    @JsonProperty(value = "erOverstyrtInntekt")
    @NotNull
    @Valid
    private boolean erOverstyrtInntekt;

    @JsonProperty(value = "vilkårsperiodeFom")
    @Valid
    private LocalDate vilkårsperiodeFom;

    @JsonProperty(value = "inntektsgrunnlag")
    @Valid
    private InntektsgrunnlagDto inntektsgrunnlag;

    @JsonProperty(value = "forlengelseperioder")
    @Valid
    @Size(max = 100)
    private List<Periode> forlengelseperioder;

    public BeregningsgrunnlagDto() {
        // trengs for deserialisering av JSON
    }

    public BeregningsgrunnlagDto(BeregningsgrunnlagDto beregningsgrunnlagDto) {
        this.aktivitetStatus = beregningsgrunnlagDto.aktivitetStatus;
        this.beregningsgrunnlagPeriode = beregningsgrunnlagDto.beregningsgrunnlagPeriode;
        this.andelerMedGraderingUtenBG = beregningsgrunnlagDto.andelerMedGraderingUtenBG;
        this.faktaOmBeregning = beregningsgrunnlagDto.faktaOmBeregning;
        this.dekningsgrad = beregningsgrunnlagDto.dekningsgrad;
        this.erOverstyrtInntekt = beregningsgrunnlagDto.erOverstyrtInntekt;
        this.faktaOmFordeling = beregningsgrunnlagDto.faktaOmFordeling;
        this.grunnbeløp = beregningsgrunnlagDto.grunnbeløp;
        this.halvG = beregningsgrunnlagDto.halvG;
        this.hjemmel = beregningsgrunnlagDto.hjemmel;
        this.refusjonTilVurdering = beregningsgrunnlagDto.refusjonTilVurdering;
        this.sammenligningsgrunnlag = beregningsgrunnlagDto.sammenligningsgrunnlag;
        this.sammenligningsgrunnlagPrStatus = beregningsgrunnlagDto.sammenligningsgrunnlagPrStatus;
        this.skjaeringstidspunktBeregning = beregningsgrunnlagDto.skjaeringstidspunktBeregning;
        this.skjæringstidspunkt = beregningsgrunnlagDto.skjæringstidspunkt;
        this.vilkårsperiodeFom = beregningsgrunnlagDto.vilkårsperiodeFom;
        this.ytelsesspesifiktGrunnlag = beregningsgrunnlagDto.ytelsesspesifiktGrunnlag;
        this.inntektsgrunnlag = beregningsgrunnlagDto.inntektsgrunnlag;
        this.forlengelseperioder = beregningsgrunnlagDto.forlengelseperioder;
    }


    public LocalDate getSkjaeringstidspunktBeregning() {
        return skjaeringstidspunktBeregning;
    }

    public List<AktivitetStatus> getAktivitetStatus() {
        return aktivitetStatus;
    }

    public List<BeregningsgrunnlagPeriodeDto> getBeregningsgrunnlagPeriode() {
        return beregningsgrunnlagPeriode;
    }

    public SammenligningsgrunnlagDto getSammenligningsgrunnlag() {
        return sammenligningsgrunnlag;
    }

    public Beløp getHalvG() {
        return halvG;
    }

    public FordelingDto getFaktaOmFordeling() {
        return faktaOmFordeling;
    }

    public FaktaOmBeregningDto getFaktaOmBeregning() {
        return faktaOmBeregning;
    }

    public Hjemmel getHjemmel() {
        return hjemmel;
    }

    public List<SammenligningsgrunnlagDto> getSammenligningsgrunnlagPrStatus() {
        return sammenligningsgrunnlagPrStatus;
    }

    public void setSkjaeringstidspunktBeregning(LocalDate skjaeringstidspunktBeregning) {
        this.skjaeringstidspunktBeregning = skjaeringstidspunktBeregning;
    }

    public void setAktivitetStatus(List<AktivitetStatus> aktivitetStatus) {
        this.aktivitetStatus = aktivitetStatus;
    }

    public void setBeregningsgrunnlagPeriode(List<BeregningsgrunnlagPeriodeDto> perioder) {
        this.beregningsgrunnlagPeriode = perioder;
    }

    public void setSammenligningsgrunnlag(SammenligningsgrunnlagDto sammenligningsgrunnlag) {
        this.sammenligningsgrunnlag = sammenligningsgrunnlag;
    }


    public void setHalvG(Beløp halvG) {
        this.halvG = halvG;
    }

    public void setFaktaOmBeregning(FaktaOmBeregningDto faktaOmBeregning) {
        this.faktaOmBeregning = faktaOmBeregning;
    }

    public List<BeregningsgrunnlagPrStatusOgAndelDto> getAndelerMedGraderingUtenBG() {
        return andelerMedGraderingUtenBG;
    }

    public void setAndelerMedGraderingUtenBG(List<BeregningsgrunnlagPrStatusOgAndelDto> andelerMedGraderingUtenBG) {
        this.andelerMedGraderingUtenBG = andelerMedGraderingUtenBG;
    }

    public void setHjemmel(Hjemmel hjemmel) {
        this.hjemmel = hjemmel;
    }

    public void setFaktaOmFordeling(FordelingDto faktaOmFordelingDto) {
        this.faktaOmFordeling = faktaOmFordelingDto;
    }

    public int getDekningsgrad() {
        return dekningsgrad;
    }

    public void setDekningsgrad(int dekningsgrad) {
        this.dekningsgrad = dekningsgrad;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public void setSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
        this.skjæringstidspunkt = skjæringstidspunkt;
    }

    public Beløp getGrunnbeløp() {
        return grunnbeløp;
    }

    public void setGrunnbeløp(Beløp grunnbeløp) {
        this.grunnbeløp = grunnbeløp;
    }

    public void setSammenligningsgrunnlagPrStatus(List<SammenligningsgrunnlagDto> sammenligningsgrunnlagPrStatus) {
        this.sammenligningsgrunnlagPrStatus = sammenligningsgrunnlagPrStatus;
    }

    public YtelsespesifiktGrunnlagDto getYtelsesspesifiktGrunnlag() {
        return ytelsesspesifiktGrunnlag;
    }

    public void setYtelsesspesifiktGrunnlag(YtelsespesifiktGrunnlagDto ytelsesspesifiktGrunnlag) {
        this.ytelsesspesifiktGrunnlag = ytelsesspesifiktGrunnlag;
    }

    public boolean isErOverstyrtInntekt() {
        return erOverstyrtInntekt;
    }

    public void setErOverstyrtInntekt(boolean erOverstyrtInntekt) {
        this.erOverstyrtInntekt = erOverstyrtInntekt;
    }

    public LocalDate getVilkårsperiodeFom() {
        return vilkårsperiodeFom;
    }

    public void setVilkårsperiodeFom(LocalDate vilkårsperiodeFom) {
        this.vilkårsperiodeFom = vilkårsperiodeFom;
    }

    public RefusjonTilVurderingDto getRefusjonTilVurdering() {
        return refusjonTilVurdering;
    }

    public void setRefusjonTilVurdering(RefusjonTilVurderingDto refusjonTilVurdering) {
        this.refusjonTilVurdering = refusjonTilVurdering;
    }

    public InntektsgrunnlagDto getInntektsgrunnlag() {
        return inntektsgrunnlag;
    }

    public void setInntektsgrunnlag(InntektsgrunnlagDto inntektsgrunnlag) {
        this.inntektsgrunnlag = inntektsgrunnlag;
    }

    public List<AvklaringsbehovDto> getAvklaringsbehov() {
        return avklaringsbehov;
    }

    public void setAvklaringsbehov(List<AvklaringsbehovDto> avklaringsbehov) {
        this.avklaringsbehov = avklaringsbehov;
    }

    public List<Periode> getForlengelseperioder() {
        return forlengelseperioder;
    }

    public void setForlengelseperioder(List<Periode> forlengelseperioder) {
        this.forlengelseperioder = forlengelseperioder;
    }
}
