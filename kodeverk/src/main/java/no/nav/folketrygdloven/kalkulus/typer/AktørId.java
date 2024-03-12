package no.nav.folketrygdloven.kalkulus.typer;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AktørId implements Serializable, Comparable<AktørId>{

    @JsonProperty(value = "aktørId")
    @NotNull
    @Pattern(regexp = "^\\d{13}+$", message = "aktørId ${validatedValue} har ikke gyldig verdi (13 siffer)")
    private String aktørId; // NOSONAR

    protected AktørId() {
        // for jpa
    }
    
    @JsonCreator
    public AktørId(@JsonProperty(value = "aktørId", required = true, index = 1) String aktørId) {
        this.aktørId = aktørId;
    }
    
    public AktørId(Long aktørId) {
        this.aktørId = Long.toString(Objects.requireNonNull(aktørId, "aktørId"));
    }

    public String getAktørId() {
        return aktørId;
    }

    public void setAktørId(String aktørId) {
        this.aktørId = aktørId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        AktørId other = (AktørId) obj;
        return Objects.equals(aktørId, other.aktørId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktørId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<********>";
    }

    public String getId() {
        return getAktørId();
    }

    @Override
    public int compareTo(AktørId o) {
        return aktørId.compareTo(o.aktørId);
    }

    private static final AtomicLong DUMMY_AKTØRID = new AtomicLong(1000000000000L);

    /** Genererer dummy aktørid unikt for test. */
    public static AktørId dummy() {
        return new AktørId(DUMMY_AKTØRID.getAndIncrement());
    }

}
