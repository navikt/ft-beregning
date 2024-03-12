package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.detaljert;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, setterVisibility = NONE, isGetterVisibility = NONE, creatorVisibility = NONE)
public class BeregningAktivitetAggregatDto {

    @JsonProperty(value = "aktiviteter")
    @Size(min = 1)
    @Valid
    private List<BeregningAktivitetDto> aktiviteter;

    @JsonProperty(value = "skjæringstidspunktOpptjening")
    @NotNull
    @Valid
    private LocalDate skjæringstidspunktOpptjening;

    public BeregningAktivitetAggregatDto() {
    }

    public BeregningAktivitetAggregatDto(@NotNull @Valid List<BeregningAktivitetDto> aktiviteter, @NotNull @Valid LocalDate skjæringstidspunktOpptjening) {
        this.aktiviteter = aktiviteter;
        this.skjæringstidspunktOpptjening = skjæringstidspunktOpptjening;
    }

    public List<BeregningAktivitetDto> getAktiviteter() {
        return aktiviteter;
    }

    public LocalDate getSkjæringstidspunktOpptjening() {
        return skjæringstidspunktOpptjening;
    }
}
