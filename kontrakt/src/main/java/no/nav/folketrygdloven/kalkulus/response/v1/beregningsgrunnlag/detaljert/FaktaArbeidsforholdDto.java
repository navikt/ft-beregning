package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class FaktaArbeidsforholdDto {

    @JsonProperty(value = "arbeidsgiver")
    @NotNull
    @Valid
    private Arbeidsgiver arbeidsgiver;

    @JsonProperty(value = "arbeidsforholdRef")
    @Valid
    private InternArbeidsforholdRefDto arbeidsforholdRef;

    @JsonProperty(value = "erTidsbegrenset")
    @Valid
    private Boolean erTidsbegrenset;

    @JsonProperty(value = "harMottattYtelse")
    @Valid
    private Boolean harMottattYtelse;

    @JsonProperty(value = "harLønnsendringIBeregningsperioden")
    @Valid
    private Boolean harLønnsendringIBeregningsperioden;

    protected FaktaArbeidsforholdDto() {
    }
    
    public FaktaArbeidsforholdDto(@NotNull @Valid Arbeidsgiver arbeidsgiver,
                                  @Valid InternArbeidsforholdRefDto arbeidsforholdRef,
                                  @Valid Boolean erTidsbegrenset,
                                  @Valid Boolean harMottattYtelse,
                                  @Valid Boolean harLønnsendringIBeregningsperioden) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.erTidsbegrenset = erTidsbegrenset;
        this.harMottattYtelse = harMottattYtelse;
        this.harLønnsendringIBeregningsperioden = harLønnsendringIBeregningsperioden;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public Boolean getErTidsbegrenset() {
        return erTidsbegrenset;
    }

    public Boolean getHarMottattYtelse() {
        return harMottattYtelse;
    }

    public Boolean getHarLønnsendringIBeregningsperioden() {
        return harLønnsendringIBeregningsperioden;
    }
}
