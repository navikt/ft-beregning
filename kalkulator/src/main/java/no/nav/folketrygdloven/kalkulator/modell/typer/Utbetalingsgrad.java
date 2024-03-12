package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.math.BigDecimal;
import java.util.Objects;

public record Utbetalingsgrad(BigDecimal verdi) implements Comparable<Utbetalingsgrad> {

    public static final Utbetalingsgrad ZERO = Utbetalingsgrad.fra(BigDecimal.ZERO);

    public Utbetalingsgrad {
        Objects.requireNonNull(verdi);
    }

    public static Utbetalingsgrad fra(BigDecimal grad) {
        return grad != null ? new Utbetalingsgrad(grad) : null;
    }

    public static Utbetalingsgrad valueOf(Integer grad) {
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
