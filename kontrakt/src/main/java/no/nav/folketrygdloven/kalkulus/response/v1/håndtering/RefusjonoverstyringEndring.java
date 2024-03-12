package no.nav.folketrygdloven.kalkulus.response.v1.håndtering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class RefusjonoverstyringEndring {

    @JsonProperty(value = "refusjonperiodeEndringer")
    @Valid
    @Size(min = 1)
    @NotNull
    private List<RefusjonoverstyringPeriodeEndring> refusjonperiodeEndringer;

    public RefusjonoverstyringEndring() {
    }

    public RefusjonoverstyringEndring(@Valid @Size(min = 1) @NotNull List<RefusjonoverstyringPeriodeEndring> refusjonperiodeEndringer) {
        this.refusjonperiodeEndringer = refusjonperiodeEndringer;
    }

    public List<RefusjonoverstyringPeriodeEndring> getRefusjonperiodeEndringer() {
        return refusjonperiodeEndringer;
    }

}
