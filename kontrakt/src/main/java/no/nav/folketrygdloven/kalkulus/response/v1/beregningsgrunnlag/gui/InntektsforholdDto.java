package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.gui;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class InntektsforholdDto {

    @JsonProperty("aktivitetStatus")
    @Valid
    @NotNull
    private AktivitetStatus aktivitetStatus;

    @JsonProperty("arbeidsgiverId")
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsgiverIdentifikator;


    @JsonProperty("arbeidsforholdId")
    @Valid
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String arbeidsforholdId;

    @Valid
    @JsonProperty(value = "eksternArbeidsforholdId")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String eksternArbeidsforholdId;

    @JsonProperty("periode")
    @Valid
    @NotNull
    private Periode periode;

    @JsonProperty("inntektFraInntektsmeldingPrÅr")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer inntektFraInntektsmeldingPrÅr;

    @JsonProperty("bruttoInntektPrÅr")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer bruttoInntektPrÅr;
    @JsonProperty("skalRedusereUtbetaling")
    @Valid
    private Boolean skalRedusereUtbetaling;

    public InntektsforholdDto(AktivitetStatus aktivitetStatus,
                              String arbeidsgiverIdentifikator,
                              String arbeidsforholdId,
                              String eksternArbeidsforholdId, Periode periode,
                              Integer inntektFraInntektsmeldingPrÅr,
                              Integer bruttoInntektPrÅr,
                              Boolean skalRedusereUtbetaling) {
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiverIdentifikator = arbeidsgiverIdentifikator;
        this.arbeidsforholdId = arbeidsforholdId;
        this.eksternArbeidsforholdId = eksternArbeidsforholdId;
        this.periode = periode;
        this.inntektFraInntektsmeldingPrÅr = inntektFraInntektsmeldingPrÅr;
        this.bruttoInntektPrÅr = bruttoInntektPrÅr;
        this.skalRedusereUtbetaling = skalRedusereUtbetaling;
    }

    public InntektsforholdDto() {
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public void setAktivitetStatus(AktivitetStatus aktivitetStatus) {
        this.aktivitetStatus = aktivitetStatus;
    }

    public String getArbeidsgiverIdentifikator() {
        return arbeidsgiverIdentifikator;
    }

    public void setArbeidsgiverIdentifikator(String arbeidsgiverIdentifikator) {
        this.arbeidsgiverIdentifikator = arbeidsgiverIdentifikator;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public void setArbeidsforholdId(String arbeidsforholdId) {
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public String getEksternArbeidsforholdId() {
        return eksternArbeidsforholdId;
    }

    public void setEksternArbeidsforholdId(String eksternArbeidsforholdId) {
        this.eksternArbeidsforholdId = eksternArbeidsforholdId;
    }

    public Integer getBruttoInntektPrÅr() {
        return bruttoInntektPrÅr;
    }

    public void setBruttoInntektPrÅr(Integer bruttoInntektPrÅr) {
        this.bruttoInntektPrÅr = bruttoInntektPrÅr;
    }

    public Boolean getSkalRedusereUtbetaling() {
        return skalRedusereUtbetaling;
    }

    public void setSkalRedusereUtbetaling(Boolean skalRedusereUtbetaling) {
        this.skalRedusereUtbetaling = skalRedusereUtbetaling;
    }

    public Periode getPeriode() {
        return periode;
    }

    public void setPeriode(Periode periode) {
        this.periode = periode;
    }

    public Integer getInntektFraInntektsmeldingPrÅr() {
        return inntektFraInntektsmeldingPrÅr;
    }

    public void setInntektFraInntektsmeldingPrÅr(Integer inntektFraInntektsmeldingPrÅr) {
        this.inntektFraInntektsmeldingPrÅr = inntektFraInntektsmeldingPrÅr;
    }


}
