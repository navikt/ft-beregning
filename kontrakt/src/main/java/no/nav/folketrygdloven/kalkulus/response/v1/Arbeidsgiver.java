package no.nav.folketrygdloven.kalkulus.response.v1;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class Arbeidsgiver {

    @JsonProperty(value = "arbeidsgiverOrgnr")
    @Valid
    @Size(max=20)
    @Pattern(regexp = "^\\d+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String arbeidsgiverOrgnr;

    @JsonProperty(value = "arbeidsgiverAktørId")
    @Valid
    @Pattern(regexp = "^\\d+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String arbeidsgiverAktørId;

    public Arbeidsgiver() {
    }

    public Arbeidsgiver(@Valid String arbeidsgiverOrgnr, @Valid String arbeidsgiverAktørId) {
        this.arbeidsgiverOrgnr = arbeidsgiverOrgnr;
        this.arbeidsgiverAktørId = arbeidsgiverAktørId;
    }

    public String getArbeidsgiverOrgnr() {
        return arbeidsgiverOrgnr;
    }

    public String getArbeidsgiverAktørId() {
        return arbeidsgiverAktørId;
    }
}
