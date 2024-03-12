package no.nav.folketrygdloven.kalkulus.request.v1.tilkommetAktivitet;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
public class UtledTilkommetAktivitetForRequest {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;


    @JsonProperty(value = "kalkulatorInput")
    @Valid
    private KalkulatorInputDto kalkulatorInput;


    @JsonCreator
    public UtledTilkommetAktivitetForRequest(@JsonProperty(value = "eksternReferanse", required = true) UUID eksternReferanse,
                                             @JsonProperty(value = "kalkulatorInput") KalkulatorInputDto kalkulatorInput) {
        this.eksternReferanse = eksternReferanse;
        this.kalkulatorInput = kalkulatorInput;
    }

    public UUID getEksternReferanse() {
        return eksternReferanse;
    }


    public KalkulatorInputDto getKalkulatorInput() {
        return kalkulatorInput;
    }
}
