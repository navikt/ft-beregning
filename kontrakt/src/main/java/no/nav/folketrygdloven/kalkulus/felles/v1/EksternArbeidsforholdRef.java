package no.nav.folketrygdloven.kalkulus.felles.v1;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ekstern arbeidsforhold referanse.
 * Mottatt fra inntektsmelding eller AARegisteret.
 *
 * Hvis null gjelder det flere arbeidsforhold, ellers for et spesifikt forhold
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class EksternArbeidsforholdRef {

    @JsonProperty(value = "referanse")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "EksternReferanse ${validatedValue} matcher ikke tillatt pattern '{regexp}'")
    @NotNull
    private String referanse;

    public EksternArbeidsforholdRef() {
        // Json serialisering
    }

    public EksternArbeidsforholdRef(String referanse) {
        this.referanse = referanse;
    }


    public String getReferanse() {
        return referanse;
    }

}
