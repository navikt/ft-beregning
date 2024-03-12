package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
@JsonTypeInfo(
        use=JsonTypeInfo.Id.NAME,
        include=JsonTypeInfo.As.PROPERTY,
        property="dtoType")
@JsonSubTypes({
        @JsonSubTypes.Type(value=BeregningsgrunnlagPrStatusOgAndelATDto.class, name= "AT"),
        @JsonSubTypes.Type(value=BeregningsgrunnlagPrStatusOgAndelSNDto.class, name= "SN"),
        @JsonSubTypes.Type(value=BeregningsgrunnlagPrStatusOgAndelFLDto.class, name= "FL"),
        @JsonSubTypes.Type(value=BeregningsgrunnlagPrStatusOgAndelYtelseDto.class, name= "KUN_YTELSE"),
        @JsonSubTypes.Type(value=BeregningsgrunnlagPrStatusOgAndelDtoFelles.class, name= "GENERELL")
})
public abstract class BeregningsgrunnlagPrStatusOgAndelDto {

    @Valid
    @JsonProperty("aktivitetStatus")
    private AktivitetStatus aktivitetStatus;

    @Valid
    @JsonProperty("beregningsperiodeFom")
    private LocalDate beregningsperiodeFom;

    @Valid
    @JsonProperty("beregningsperiodeTom")
    private LocalDate beregningsperiodeTom;

    @Valid
    @JsonProperty("beregnetPrAar")
    private Beløp beregnetPrAar;

    @Valid
    @JsonProperty("overstyrtPrAar")
    private Beløp overstyrtPrAar;

    @Valid
    @JsonProperty("bruttoPrAar")
    private Beløp bruttoPrAar;

    @Valid
    @JsonProperty("avkortetPrAar")
    private Beløp avkortetPrAar;

    @Valid
    @JsonProperty("redusertPrAar")
    private Beløp redusertPrAar;

    @Valid
    @JsonProperty("erTidsbegrensetArbeidsforhold")
    private Boolean erTidsbegrensetArbeidsforhold;

    @Valid
    @JsonProperty("erNyIArbeidslivet")
    private Boolean erNyIArbeidslivet;

    @Valid
    @JsonProperty("lonnsendringIBeregningsperioden")
    private Boolean lonnsendringIBeregningsperioden;

    @Valid
    @JsonProperty("andelsnr")
    @Min(1)
    @Max(1000)
    private Long andelsnr;

    @Valid
    @JsonProperty("besteberegningPrAar")
    private Beløp besteberegningPrAar;

    @Valid
    @JsonProperty("inntektskategori")
    private Inntektskategori inntektskategori;

    @Valid
    @JsonProperty("arbeidsforhold")
    private BeregningsgrunnlagArbeidsforholdDto arbeidsforhold;

    @Valid
    @JsonProperty("fastsattAvSaksbehandler")
    private Boolean fastsattAvSaksbehandler;

    @Valid
    @JsonProperty("lagtTilAvSaksbehandler")
    private Boolean lagtTilAvSaksbehandler;

    @Valid
    @JsonProperty("belopPrMndEtterAOrdningen")
    private Beløp belopPrMndEtterAOrdningen;

    @Valid
    @JsonProperty("belopPrAarEtterAOrdningen")
    private Beløp belopPrAarEtterAOrdningen;

    @Valid
    @JsonProperty("dagsats")
    @Min(0)
    @Max(1000000)
    private Long dagsats;

    @Valid
    @Min(0)
    @Max(1000000)
    @JsonProperty("originalDagsatsFraTilstøtendeYtelse")
    private Long originalDagsatsFraTilstøtendeYtelse;

    @Valid
    @JsonProperty("fordeltPrAar")
    private Beløp fordeltPrAar;

    @Valid
    @JsonProperty("erTilkommetAndel")
    private Boolean erTilkommetAndel;

    @Valid
    @JsonProperty("skalFastsetteGrunnlag")
    private Boolean skalFastsetteGrunnlag;

    public BeregningsgrunnlagPrStatusOgAndelDto() {
        // trengs for deserialisering av JSON
    }

    public Beløp getBeregnetPrAar() {
        return beregnetPrAar;
    }

    public Beløp getOverstyrtPrAar() {
        return overstyrtPrAar;
    }

    public Beløp getBruttoPrAar() {
        return bruttoPrAar;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Beløp getAvkortetPrAar() {
        return avkortetPrAar;
    }

    public Beløp getRedusertPrAar() {
        return redusertPrAar;
    }

    public Boolean getErTidsbegrensetArbeidsforhold() {
        return erTidsbegrensetArbeidsforhold;
    }

    public Boolean getErNyIArbeidslivet() {
        return erNyIArbeidslivet;
    }

    public Long getAndelsnr() {
        return andelsnr;
    }

    public Boolean getLonnsendringIBeregningsperioden() {
        return lonnsendringIBeregningsperioden;
    }

    public Beløp getBesteberegningPrAar() {
        return besteberegningPrAar;
    }

    public BeregningsgrunnlagArbeidsforholdDto getArbeidsforhold() {
        return arbeidsforhold;
    }

    public Beløp getFordeltPrAar() {
        return fordeltPrAar;
    }

    public Boolean getSkalFastsetteGrunnlag() {
        return skalFastsetteGrunnlag;
    }

    public LocalDate getBeregningsperiodeFom() {
        return beregningsperiodeFom;
    }

    public LocalDate getBeregningsperiodeTom() {
        return beregningsperiodeTom;
    }

    public void setFordeltPrAar(Beløp fordeltPrAar) {
        this.fordeltPrAar = fordeltPrAar;
    }

    public void setArbeidsforhold(BeregningsgrunnlagArbeidsforholdDto arbeidsforhold) {
        this.arbeidsforhold = arbeidsforhold;
    }

    public void setBesteberegningPrAar(Beløp besteberegningPrAar) {
        this.besteberegningPrAar = besteberegningPrAar;
    }

    public void setLonnsendringIBeregningsperioden(Boolean lonnsendringIBeregningsperioden) {
        this.lonnsendringIBeregningsperioden = lonnsendringIBeregningsperioden;
    }

    public void setBeregningsperiodeFom(LocalDate beregningsperiodeFom) {
        this.beregningsperiodeFom = beregningsperiodeFom;
    }

    public void setBeregningsperiodeTom(LocalDate beregningsperiodeTom) {
        this.beregningsperiodeTom = beregningsperiodeTom;
    }

    public void setBeregnetPrAar(Beløp beregnetPrAar) {
        this.beregnetPrAar = beregnetPrAar;
    }

    public void setOverstyrtPrAar(Beløp overstyrtPrAar) {
        this.overstyrtPrAar = overstyrtPrAar;
    }

    public void setBruttoPrAar(Beløp bruttoPrAar) {
        this.bruttoPrAar = bruttoPrAar;
    }

    public void setAktivitetStatus(AktivitetStatus aktivitetStatus) {
        this.aktivitetStatus = aktivitetStatus;
    }

    public void setAvkortetPrAar(Beløp avkortetPrAar) {
        this.avkortetPrAar = avkortetPrAar;
    }

    public void setRedusertPrAar(Beløp redusertPrAar) {
        this.redusertPrAar = redusertPrAar;
    }

    public void setErTidsbegrensetArbeidsforhold(Boolean erTidsbegrensetArbeidsforhold) {
        this.erTidsbegrensetArbeidsforhold = erTidsbegrensetArbeidsforhold;
    }

    public void setAndelsnr(Long andelsnr) {
        this.andelsnr = andelsnr;
    }

    public void setErNyIArbeidslivet(Boolean erNyIArbeidslivet) {
        this.erNyIArbeidslivet = erNyIArbeidslivet;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public void setInntektskategori(Inntektskategori inntektskategori) {
        this.inntektskategori = inntektskategori;
    }

    public Boolean getFastsattAvSaksbehandler() {
        return fastsattAvSaksbehandler;
    }

    public void setFastsattAvSaksbehandler(Boolean fastsattAvSaksbehandler) {
        this.fastsattAvSaksbehandler = fastsattAvSaksbehandler;
    }

    public Boolean getLagtTilAvSaksbehandler() {
        return lagtTilAvSaksbehandler;
    }

    public void setLagtTilAvSaksbehandler(Boolean lagtTilAvSaksbehandler) {
        this.lagtTilAvSaksbehandler = lagtTilAvSaksbehandler;
    }

    public Long getDagsats() {
        return dagsats;
    }

    public void setDagsats(Long dagsats) {
        this.dagsats = dagsats;
    }

    public Long getOriginalDagsatsFraTilstøtendeYtelse() {
        return originalDagsatsFraTilstøtendeYtelse;
    }

    public void setOriginalDagsatsFraTilstøtendeYtelse(Long originalDagsatsFraTilstøtendeYtelse) {
        this.originalDagsatsFraTilstøtendeYtelse = originalDagsatsFraTilstøtendeYtelse;
    }

    public Boolean getErTilkommetAndel() {
        return erTilkommetAndel;
    }

    public void setErTilkommetAndel(Boolean erTilkommetAndel) {
        this.erTilkommetAndel = erTilkommetAndel;
    }

    public void setSkalFastsetteGrunnlag(Boolean skalFastsetteGrunnlag) {
        this.skalFastsetteGrunnlag = skalFastsetteGrunnlag;
    }
}
