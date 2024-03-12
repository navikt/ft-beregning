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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class OpplæringspengerGrunnlag extends YtelsespesifiktGrunnlagDto {

    @JsonProperty(value = "utbetalingsgradPrAktivitet", required = true)
    @Size()
    @Valid
    private List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet;

    protected OpplæringspengerGrunnlag() {
        // default ctor
    }

    public OpplæringspengerGrunnlag(@NotNull @Valid List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitet) {
        this.utbetalingsgradPrAktivitet = utbetalingsgradPrAktivitet;
    }

    public List<UtbetalingsgradPrAktivitetDto> getUtbetalingsgradPrAktivitet() {
        return utbetalingsgradPrAktivitet;
    }

    @Override
    public String toString() {
        return "OpplæringspengerGrunnlag{" +
                "utbetalingsgradPrAktivitet=" + utbetalingsgradPrAktivitet +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OpplæringspengerGrunnlag that = (OpplæringspengerGrunnlag) o;
        return Objects.equals(utbetalingsgradPrAktivitet, that.utbetalingsgradPrAktivitet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), utbetalingsgradPrAktivitet);
    }
}
