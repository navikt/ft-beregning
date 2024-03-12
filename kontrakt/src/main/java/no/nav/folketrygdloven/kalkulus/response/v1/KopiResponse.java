package no.nav.folketrygdloven.kalkulus.response.v1;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

/**
 * Beskriver hvilke avklaringsbehov som må løses av K9 eller FPSAK for at beregningen kan fortsette
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class KopiResponse implements KalkulusRespons {

    @JsonProperty(value = "eksternReferanse")
    @Valid
    private UUID eksternReferanse;


    public KopiResponse() {
        // default ctor
    }

    public KopiResponse(@JsonProperty(value = "eksternReferanse") @Valid UUID eksternReferanse) {
        this.eksternReferanse = eksternReferanse;

    }

    @Override
    public UUID getEksternReferanse() {
        return eksternReferanse;
    }

}
