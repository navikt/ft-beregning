package no.nav.folketrygdloven.kalkulus.felles.v1;


import java.util.Objects;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Denne referansen blir generet av Abakus og er en intern referanse som blir brukt internt av alle kombonenter i
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class InternArbeidsforholdRefDto {

    @JsonProperty(value = "abakusReferanse")
    @Pattern(regexp = "^[\\p{L}\\p{N}_\\.\\-]+$", message = "Abakusreferanse ${validatedValue} matcher ikke tillatt pattern '{regexp}'")
    @NotNull
    private String abakusReferanse;

    @JsonCreator
    public InternArbeidsforholdRefDto(@JsonProperty(value = "abakusReferanse", required = true) String internReferanse) {
        this.abakusReferanse = internReferanse;
    }

    public String getAbakusReferanse() {
        return abakusReferanse;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<abakusRef=" + getAbakusReferanse();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        var other = getClass().cast(obj);
        return Objects.equals(this.abakusReferanse, other.abakusReferanse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(abakusReferanse);
    }
}
