package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningsgrunnlagPrStatusOgAndelFRISINNDto {

    @JsonProperty(value = "aktivitetStatus")
    @NotNull
    @Valid
    private AktivitetStatus aktivitetStatus;

    @JsonProperty(value = "bruttoPrÅr")
    @NotNull
    @Valid
    private Beløp bruttoPrÅr;

    @JsonProperty(value = "redusertPrÅr")
    @NotNull
    @Valid
    private Beløp redusertPrÅr;

    @JsonProperty(value = "avkortetPrÅr")
    @NotNull
    @Valid
    private Beløp avkortetPrÅr;

    @JsonProperty(value = "løpendeInntektPrÅr")
    @NotNull
    @Valid
    private Beløp løpendeInntektPrÅr;

    @JsonProperty(value = "bgFratrukketInntektstak")
    @NotNull
    @Valid
    private Beløp bgFratrukketInntektstak;

    @JsonProperty(value = "dagsats")
    @NotNull
    @Valid
    @Min(0)
    @Max(178956970)
    private Long dagsats;

    @JsonProperty(value = "inntektskategori")
    @NotNull
    @Valid
    private Inntektskategori inntektskategori;

    @JsonProperty(value = "avslagsårsak")
    @Valid
    private Avslagsårsak avslagsårsak;

    public BeregningsgrunnlagPrStatusOgAndelFRISINNDto(@NotNull @Valid AktivitetStatus aktivitetStatus,
                                                       @NotNull @Valid Beløp bruttoPrÅr,
                                                       @NotNull @Valid Beløp redusertPrÅr,
                                                       @NotNull @Valid Beløp avkortetPrÅr,
                                                       @NotNull @Valid Beløp løpendeInntektPrÅr,
                                                       @NotNull @Valid Beløp bgFratrukketInntektstak,
                                                       @NotNull @Valid @Min(0) @Max(178956970) Long dagsats,
                                                       @NotNull @Valid Inntektskategori inntektskategori, Avslagsårsak avslagsårsak) {
        this.aktivitetStatus = aktivitetStatus;
        this.bruttoPrÅr = bruttoPrÅr;
        this.redusertPrÅr = redusertPrÅr;
        this.avkortetPrÅr = avkortetPrÅr;
        this.løpendeInntektPrÅr = løpendeInntektPrÅr;
        this.dagsats = dagsats;
        this.inntektskategori = inntektskategori;
        this.avslagsårsak = avslagsårsak;
        this.bgFratrukketInntektstak = bgFratrukketInntektstak;
    }

    public BeregningsgrunnlagPrStatusOgAndelFRISINNDto() {
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Beløp getBruttoPrÅr() {
        return bruttoPrÅr;
    }

    public Beløp getRedusertPrÅr() {
        return redusertPrÅr;
    }

    public Beløp getAvkortetPrÅr() {
        return avkortetPrÅr;
    }

    public Beløp getLøpendeInntektPrÅr() {
        return løpendeInntektPrÅr;
    }

    public Long getDagsats() {
        return dagsats;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Avslagsårsak getAvslagsårsak() {
        return avslagsårsak;
    }

    public Beløp getBgFratrukketInntektstak() {
        return bgFratrukketInntektstak;
    }
}
