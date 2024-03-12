package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.math.BigDecimal;
import java.util.Objects;

public record Aktivitetsgrad(BigDecimal verdi) implements Comparable<Aktivitetsgrad> {

    public static final Aktivitetsgrad ZERO = Aktivitetsgrad.fra(BigDecimal.ZERO);
    public static final Aktivitetsgrad HUNDRE = Aktivitetsgrad.fra(BigDecimal.valueOf(100));

    public Aktivitetsgrad {
        Objects.requireNonNull(verdi);
    }

    public static Aktivitetsgrad fra(BigDecimal grad) {
        return grad != null ? new Aktivitetsgrad(grad) : null;
    }

    public static Aktivitetsgrad fra(Integer grad) {
        return grad != null ? new Aktivitetsgrad(BigDecimal.valueOf(grad)) : null;
    }

    public Aktivitetsgrad subtraher(Aktivitetsgrad operand) {
        return new Aktivitetsgrad(this.verdi.subtract(operand.verdi()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Aktivitetsgrad ob && (Objects.equals(this.verdi(), ob.verdi()) || (this.verdi() != null && ob.verdi() != null && this.compareTo(ob) == 0));

    }

    @Override
    public int hashCode() {
        return Objects.hash(verdi);
    }

    @Override
    public int compareTo(Aktivitetsgrad beløp) {
        return this.verdi.compareTo(beløp.verdi());
    }

}
