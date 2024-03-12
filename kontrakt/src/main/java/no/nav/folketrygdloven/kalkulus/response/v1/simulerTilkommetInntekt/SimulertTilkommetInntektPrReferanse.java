package no.nav.folketrygdloven.kalkulus.response.v1.simulerTilkommetInntekt;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class SimulertTilkommetInntektPrReferanse {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;

    @JsonProperty(value = "tilkommetAktivitetPerioder")
    @Valid
    private List<Periode> tilkommetAktivitetPerioder;

    @JsonCreator
    public SimulertTilkommetInntektPrReferanse(@JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UUID eksternReferanse,
                                               @JsonProperty(value = "tilkommetAktivitetPerioder") @Valid List<Periode> tilkommetAktivitetPerioder) {
        this.eksternReferanse = eksternReferanse;
        this.tilkommetAktivitetPerioder = tilkommetAktivitetPerioder;
    }

    public UUID getEksternReferanse() {
        return eksternReferanse;
    }

    public List<Periode> getTilkommetAktivitetPerioder() {
        return tilkommetAktivitetPerioder;
    }
}
