package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.List;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class UtbetalingsgradPrAktivitetDto {

    @JsonProperty(value = "utbetalingsgradArbeidsforholdDto", required = true)
    @Valid
    @NotNull
    private AktivitetDto utbetalingsgradArbeidsforholdDto;

    @JsonProperty(value = "periodeMedUtbetalingsgrad", required = true)
    @Valid
    @NotNull
    @Size(min = 1)
    private List<PeriodeMedUtbetalingsgradDto> periodeMedUtbetalingsgrad;

    public UtbetalingsgradPrAktivitetDto() {
    }

    public UtbetalingsgradPrAktivitetDto(@Valid @NotNull AktivitetDto utbetalingsgradArbeidsforholdDto, @Valid @NotNull @NotEmpty List<PeriodeMedUtbetalingsgradDto> periodeMedUtbetalingsgrad) {
        this.utbetalingsgradArbeidsforholdDto = utbetalingsgradArbeidsforholdDto;
        this.periodeMedUtbetalingsgrad = periodeMedUtbetalingsgrad;
    }

    public List<PeriodeMedUtbetalingsgradDto> getPeriodeMedUtbetalingsgrad() {
        return periodeMedUtbetalingsgrad;
    }

    public AktivitetDto getUtbetalingsgradArbeidsforholdDto() {
        return utbetalingsgradArbeidsforholdDto;
    }

    @Override
    public String toString() {
        return "UtbetalingsgradPrAktivitetDto{" +
                "utbetalingsgradArbeidsforholdDto=" + utbetalingsgradArbeidsforholdDto +
                ", periodeMedUtbetalingsgrad=" + periodeMedUtbetalingsgrad +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UtbetalingsgradPrAktivitetDto that = (UtbetalingsgradPrAktivitetDto) o;
        return Objects.equals(utbetalingsgradArbeidsforholdDto, that.utbetalingsgradArbeidsforholdDto) &&
                Objects.equals(periodeMedUtbetalingsgrad, that.periodeMedUtbetalingsgrad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(utbetalingsgradArbeidsforholdDto, periodeMedUtbetalingsgrad);
    }
}
