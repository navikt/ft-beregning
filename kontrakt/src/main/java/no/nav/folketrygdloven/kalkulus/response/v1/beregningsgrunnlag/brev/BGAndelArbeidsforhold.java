package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.brev;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BGAndelArbeidsforhold {

    @JsonProperty(value = "arbeidsgiver")
    @NotNull
    @Valid
    private Arbeidsgiver arbeidsgiver;

    @JsonProperty(value = "arbeidsforholdRef")
    @Valid
    private UUID arbeidsforholdRef;


    @JsonProperty(value = "refusjonskravPrÅr")
    @Valid
    private Beløp refusjonskravPrÅr;


    public BGAndelArbeidsforhold() {
    }


    public BGAndelArbeidsforhold(@NotNull @Valid Arbeidsgiver arbeidsgiver, @Valid UUID arbeidsforholdRef, @Valid Beløp refusjonskravPrÅr) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.refusjonskravPrÅr = refusjonskravPrÅr;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public UUID getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public Beløp getRefusjonskravPrÅr() {
        return refusjonskravPrÅr;
    }
}
