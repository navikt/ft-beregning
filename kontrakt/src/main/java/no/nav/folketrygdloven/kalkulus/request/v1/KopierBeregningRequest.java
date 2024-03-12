package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
public class KopierBeregningRequest {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;

    @JsonProperty(value = "kopierFraReferanse", required = true)
    @Valid
    @NotNull
    private UUID kopierFraReferanse;

    public KopierBeregningRequest() {
    }

    @JsonCreator
    public KopierBeregningRequest(@JsonProperty(value = "eksternReferanse", required = true) UUID eksternReferanse,
                                  @JsonProperty(value = "kopierFraReferanse", required = true) UUID kopierFraReferanse) {
        this.eksternReferanse = eksternReferanse;
        this.kopierFraReferanse = kopierFraReferanse;
    }

    public UUID getEksternReferanse() {
        return eksternReferanse;
    }

    public UUID getKopierFraReferanse() {
        return kopierFraReferanse;
    }

    @AssertTrue(message = "Kan ikke ha kopierFraReferanse lik eksternReferanse")
    public boolean isSkalVereUlikeReferanser() {
        return !kopierFraReferanse.equals(eksternReferanse);
    }


}
