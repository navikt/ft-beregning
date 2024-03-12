package no.nav.folketrygdloven.kalkulus.håndtering.v1.fordeling;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.response.v1.TilstandResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = NON_ABSENT, content = NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class NyttInntektsforholdDto {

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

    @JsonProperty("bruttoInntektPrÅr")
    @Valid
    @Min(0)
    @Max(178956970)
    private Integer bruttoInntektPrÅr;


    @JsonProperty("skalRedusereUtbetaling")
    @NotNull
    private boolean skalRedusereUtbetaling;


    public NyttInntektsforholdDto() {
    }

    public NyttInntektsforholdDto(AktivitetStatus aktivitetStatus,
                                  String arbeidsgiverIdentifikator,
                                  String arbeidsforholdId,
                                  Integer bruttoInntektPrÅr,
                                  boolean skalRedusereUtbetaling) {
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiverIdentifikator = arbeidsgiverIdentifikator;
        this.arbeidsforholdId = arbeidsforholdId;
        this.bruttoInntektPrÅr = bruttoInntektPrÅr;
        this.skalRedusereUtbetaling = skalRedusereUtbetaling;
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

    public Integer getBruttoInntektPrÅr() {
        return bruttoInntektPrÅr;
    }

    public void setBruttoInntektPrÅr(Integer bruttoInntektPrÅr) {
        this.bruttoInntektPrÅr = bruttoInntektPrÅr;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public void setArbeidsforholdId(String arbeidsforholdId) {
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public boolean getSkalRedusereUtbetaling() {
        return skalRedusereUtbetaling;
    }

    public void setSkalRedusereUtbetaling(boolean skalRedusereUtbetaling) {
        this.skalRedusereUtbetaling = skalRedusereUtbetaling;
    }

    @AssertTrue(message = "Brutto må være satt dersom inntekten skal redusere grunnlaget")
    public boolean isHarBruttoDersomSkalRedusere() {
        return  !skalRedusereUtbetaling|| bruttoInntektPrÅr != null;
    }

}
