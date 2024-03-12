package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.folketrygdloven.kalkulus.felles.v1.Aktør;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class ErTidsbegrensetArbeidsforholdEndring {

    @JsonProperty(value = "arbeidsgiver")
    @NotNull
    @Valid
    private Aktør arbeidsgiver;

    @JsonProperty(value = "arbeidsforholdRef")
    @Valid
    private UUID arbeidsforholdRef;

    @JsonProperty(value = "erTidsbegrensetArbeidsforholdEndring")
    @NotNull
    @Valid
    private ToggleEndring erTidsbegrensetArbeidsforholdEndring;

    public ErTidsbegrensetArbeidsforholdEndring() {
        // For json deserialisering
    }


    public ErTidsbegrensetArbeidsforholdEndring(@Valid @NotNull Aktør arbeidsgiver,
                                                 @Valid UUID arbeidsforholdRef,
                                                 @NotNull @Valid ToggleEndring erTidsbegrensetArbeidsforholdEndring) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.erTidsbegrensetArbeidsforholdEndring = erTidsbegrensetArbeidsforholdEndring;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public UUID getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public ToggleEndring getErTidsbegrensetArbeidsforholdEndring() {
        return erTidsbegrensetArbeidsforholdEndring;
    }
}
