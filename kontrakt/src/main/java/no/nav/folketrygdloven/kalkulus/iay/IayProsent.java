package no.nav.folketrygdloven.kalkulus.iay;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

/**
 * Skal dekke stillingsprosenter fra AA-register langt over 100, samt utbetalinger på 200% fra enkelte system
 */
public record IayProsent(@JsonValue
                         @Valid @NotNull
                         @DecimalMin(value = "0.00")
                         @DecimalMax(value = "1000.00") // Historisk arv fra bl.a AA-register
                         @Digits(integer = 3, fraction = 2)
                         BigDecimal verdi) implements Comparable<IayProsent> {

    public static final IayProsent ZERO = IayProsent.fra(BigDecimal.ZERO);

    public IayProsent {
        Objects.requireNonNull(verdi);
        if (verdi.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Negativ prosent");
        }
    }

    @JsonCreator
    public static IayProsent fra(BigDecimal grad) {
        return grad != null ? new IayProsent(grad) : null;
    }

    public static IayProsent fra(Integer grad) {
        return grad != null ? new IayProsent(BigDecimal.valueOf(grad)) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof IayProsent ob && (Objects.equals(this.verdi(), ob.verdi()) || (this.verdi() != null && ob.verdi() != null && this.compareTo(ob) == 0));

    }

    @Override
    public int hashCode() {
        return Objects.hash(verdi);
    }

    @Override
    public int compareTo(IayProsent beløp) {
        return this.verdi.compareTo(beløp.verdi());
    }


}
