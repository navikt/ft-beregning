package no.nav.folketrygdloven.kalkulus.beregning.v1;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;

/**
 * Definerer perioder der bruker har søkt
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class SøknadsperioderPrAktivitetDto {

    @JsonProperty(value = "aktivitetDto", required = true)
    @Valid
    @NotNull
    private AktivitetDto aktivitetDto;

    @JsonProperty(value = "perioder", required = true)
    @Valid
    @NotNull
    @Size(min = 1)
    private List<Periode> perioder;

    public SøknadsperioderPrAktivitetDto() {
    }

    public SøknadsperioderPrAktivitetDto(@Valid @NotNull AktivitetDto aktivitetDto, @Valid @NotNull @NotEmpty List<Periode> perioder) {
        this.aktivitetDto = aktivitetDto;
        this.perioder = perioder;
    }

    public List<Periode> getPerioder() {
        return perioder;
    }

    public AktivitetDto getAktivitet() {
        return aktivitetDto;
    }

    @Override
    public String toString() {
        return "UtbetalingsgradPrAktivitetDto{" +
                "aktivitetDto=" + aktivitetDto +
                ", perioder=" + perioder +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SøknadsperioderPrAktivitetDto that = (SøknadsperioderPrAktivitetDto) o;
        return Objects.equals(aktivitetDto, that.aktivitetDto) &&
                Objects.equals(perioder, that.perioder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitetDto, perioder);
    }
}
