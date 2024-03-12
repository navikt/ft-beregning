package no.nav.folketrygdloven.kalkulus.response.v1;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class EksternReferanseDto implements KalkulusRespons {

    @JsonProperty(value = "eksternReferanse")
    @Valid
    @NotNull
    private UUID eksternReferanse;


    public EksternReferanseDto() {
        // default ctor
    }

    public EksternReferanseDto(@JsonProperty(value = "eksternReferanse") @Valid UUID eksternReferanse) {
        this.eksternReferanse = eksternReferanse;

    }


    @Override
    public UUID getEksternReferanse() {
        return eksternReferanse;
    }
}
