package no.nav.folketrygdloven.kalkulus.response.v1;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AktiveReferanser {

    @JsonProperty(value = "referanser")
    @Valid
    @NotNull
    @Size(min = 0, max = 1000)
    private List<EksternReferanseDto> referanser;

    public AktiveReferanser() {
    }

    public AktiveReferanser(@JsonProperty(value = "referanser") @Valid List<EksternReferanseDto> referanser) {
        this.referanser = referanser;
    }

    public List<EksternReferanseDto> getReferanser() {
        return referanser;
    }
}
