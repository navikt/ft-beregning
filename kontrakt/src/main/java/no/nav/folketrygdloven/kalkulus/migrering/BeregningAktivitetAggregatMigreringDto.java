package no.nav.folketrygdloven.kalkulus.migrering;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BeregningAktivitetAggregatMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
    @Size(max=100)
    private List<BeregningAktivitetMigreringDto> aktiviteter;
    @Valid
    @NotNull
    private LocalDate skjæringstidspunktOpptjening;

    public BeregningAktivitetAggregatMigreringDto() {
    }

    public BeregningAktivitetAggregatMigreringDto(List<BeregningAktivitetMigreringDto> aktiviteter, LocalDate skjæringstidspunktOpptjening) {
        this.aktiviteter = aktiviteter;
        this.skjæringstidspunktOpptjening = skjæringstidspunktOpptjening;
    }

    public List<BeregningAktivitetMigreringDto> getAktiviteter() {
        return aktiviteter;
    }

    public LocalDate getSkjæringstidspunktOpptjening() {
        return skjæringstidspunktOpptjening;
    }
}
