package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev;

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
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class TilkommetInntektsforholdDto {

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
    @Valid
    private Boolean skalRedusereUtbetaling;

    public TilkommetInntektsforholdDto(AktivitetStatus aktivitetStatus,
                                       String arbeidsgiverIdentifikator,
                                       String arbeidsforholdId,
                                       Integer bruttoInntektPrÅr,
                                       Boolean skalRedusereUtbetaling) {
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiverIdentifikator = arbeidsgiverIdentifikator;
        this.arbeidsforholdId = arbeidsforholdId;
        this.bruttoInntektPrÅr = bruttoInntektPrÅr;
        this.skalRedusereUtbetaling = skalRedusereUtbetaling;
    }

    public TilkommetInntektsforholdDto() {
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public String getArbeidsgiverIdentifikator() {
        return arbeidsgiverIdentifikator;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public Integer getBruttoInntektPrÅr() {
        return bruttoInntektPrÅr;
    }
    public Boolean getSkalRedusereUtbetaling() {
        return skalRedusereUtbetaling;
    }

}
