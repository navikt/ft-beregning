package no.nav.folketrygdloven.kalkulus.felles.v1;

import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record Utbetalingsgrad(@JsonValue
                    @Valid
                    @NotNull
                    @DecimalMin(value = "0.00")
                    @DecimalMax(value = "100.00")
                    @Digits(integer = 3, fraction = 2)
                    BigDecimal verdi) implements Comparable<Utbetalingsgrad> {

    public static final Utbetalingsgrad ZERO = Utbetalingsgrad.fra(BigDecimal.ZERO);

    public Utbetalingsgrad {
        Objects.requireNonNull(verdi);
    }

    @JsonCreator
    public static Utbetalingsgrad fra(BigDecimal grad) {
        return grad != null ? new Utbetalingsgrad(grad) : null;
    }

    public static Utbetalingsgrad fra(Integer grad) {
        return grad != null ? new Utbetalingsgrad(BigDecimal.valueOf(grad)) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Utbetalingsgrad ob && (Objects.equals(this.verdi(), ob.verdi()) || (this.verdi() != null && ob.verdi() != null && this.compareTo(ob) == 0));

    }

    @Override
    public int hashCode() {
        return Objects.hash(verdi);
    }

    @Override
    public int compareTo(Utbetalingsgrad beløp) {
        return this.verdi.compareTo(beløp.verdi());
    }

}
