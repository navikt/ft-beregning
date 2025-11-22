package no.nav.folketrygdloven.kalkulus.response.v1.gradering;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class InntektgraderingPrReferanse {

    @JsonProperty(value = "eksternReferanse", required = true)
    @Valid
    @NotNull
    private UUID eksternReferanse;

    @JsonProperty(value = "tilkommetAktivitetPerioder")
    private List<@Valid InntektGraderingPeriode> perioder;

    @JsonCreator
    public InntektgraderingPrReferanse(@JsonProperty(value = "eksternReferanse", required = true) @Valid @NotNull UUID eksternReferanse,
                                       @JsonProperty(value = "perioder") List<@Valid InntektGraderingPeriode> perioder) {
        this.eksternReferanse = eksternReferanse;
        this.perioder = perioder;
    }

    public UUID getEksternReferanse() {
        return eksternReferanse;
    }

    public List<InntektGraderingPeriode> getPerioder() {
        return perioder;
    }
}
