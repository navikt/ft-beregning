package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.felles.v1.Utbetalingsgrad;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class PeriodeMedUtbetalingsgradDto {

    @JsonProperty(value = "periode", required = true)
    @NotNull
    @Valid
    private Periode periode;

    @JsonProperty(value = "utbetalingsgrad", required = true)
    @Valid
    private Utbetalingsgrad utbetalingsgrad;

    @JsonProperty(value = "aktivitetsgrad")
    @Valid
    private Aktivitetsgrad aktivitetsgrad;

    public PeriodeMedUtbetalingsgradDto() {
    }

    public PeriodeMedUtbetalingsgradDto(@NotNull @Valid Periode periode, @Valid @DecimalMin(value = "0.00", message = "utbetalingsgrad ${validatedValue} må være >= {value}") @DecimalMax(value = "100.00", message = "utbetalingsgrad ${validatedValue} må være <= {value}") Utbetalingsgrad utbetalingsgrad) {
        this.periode = periode;
        this.utbetalingsgrad = utbetalingsgrad;
    }

    public PeriodeMedUtbetalingsgradDto(@NotNull @Valid Periode periode,
                                        @Valid @DecimalMin(value = "0.00", message = "utbetalingsgrad ${validatedValue} må være >= {value}") @DecimalMax(value = "100.00", message = "utbetalingsgrad ${validatedValue} må være <= {value}") Utbetalingsgrad utbetalingsgrad,
                                        @Valid @DecimalMin(value = "0.00", message = "aktivitetsgrad ${validatedValue} må være >= {value}") @DecimalMax(value = "100.00", message = "aktivitetsgrad ${validatedValue} må være <= {value}") Aktivitetsgrad aktivitetsgrad) {
        this.periode = periode;
        this.utbetalingsgrad = utbetalingsgrad;
        this.aktivitetsgrad = aktivitetsgrad;
    }

    public Periode getPeriode() {
        return periode;
    }

    public Utbetalingsgrad getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public Aktivitetsgrad getAktivitetsgrad() {
        return aktivitetsgrad;
    }

    @Override
    public String toString() {
        return "PeriodeMedUtbetalingsgradDto{" +
                "periode=" + periode +
                ", utbetalingsgrad=" + utbetalingsgrad +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeriodeMedUtbetalingsgradDto that = (PeriodeMedUtbetalingsgradDto) o;
        return Objects.equals(periode, that.periode) &&
                Objects.equals(utbetalingsgrad, that.utbetalingsgrad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, utbetalingsgrad);
    }
}

