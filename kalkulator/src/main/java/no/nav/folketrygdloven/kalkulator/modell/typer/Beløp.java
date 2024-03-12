package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public record Beløp(BigDecimal verdi) implements Comparable<Beløp> {

    public static final Beløp ZERO = Beløp.fra(BigDecimal.ZERO);

    public Beløp {
        Objects.requireNonNull(verdi);
    }

    public boolean erNullEller0() {
        return verdi == null || this.compareTo(ZERO) == 0;
    }

    public Beløp multipliser(int operand) {
        return multipliser(BigDecimal.valueOf(operand));
    }

    public Beløp multipliser(BigDecimal operand) {
        return new Beløp(this.verdi.multiply(operand));
    }

    public Beløp multipliser(Beløp operand) {
        return multipliser(operand.verdi());
    }

    public Beløp divider(int operand, int scale, RoundingMode roundingMode) {
        return divider(BigDecimal.valueOf(operand), scale, roundingMode);
    }

    public Beløp divider(BigDecimal operand, int scale, RoundingMode roundingMode) {
        return new Beløp(this.verdi.divide(operand, scale, roundingMode));
    }

    public Beløp divider(Beløp operand, int scale, RoundingMode roundingMode) {
        return divider(operand.verdi(), scale, roundingMode);
    }

    public Beløp adder(Beløp operand) {
        return new Beløp(this.verdi.add(operand.verdi()));
    }

    public Beløp subtraher(Beløp operand) {
        return new Beløp(this.verdi.subtract(operand.verdi()));
    }

    public Beløp min(Beløp val) {
        return (compareTo(val) <= 0 ? this : val);
    }

    public Beløp max(Beløp val) {
        return (compareTo(val) >= 0 ? this : val);
    }

    public Beløp map(Function<BigDecimal, BigDecimal> mapper) {
        return Beløp.fra(mapper.apply(verdi));
    }

    public Beløp filter(Predicate<BigDecimal> filter) {
        return filter.test(verdi) ? this : null;
    }

    public static BigDecimal safeVerdi(Beløp beløp) {
        return beløp == null ? null : beløp.verdi();
    }

    public static Beløp safeSum(Beløp lhs, Beløp rhs) {
        if (Beløp.safeVerdi(lhs) == null && Beløp.safeVerdi(rhs) == null) {
            return null;
        } else if (Beløp.safeVerdi(lhs) == null) {
            return rhs;
        } else {
            return Beløp.safeVerdi(rhs) == null ? lhs : lhs.adder(rhs);
        }
    }

    public static Beløp fra(BigDecimal beløp) {
        return beløp != null ? new Beløp(beløp) : null;
    }

    public static Beløp fra(Long beløp) {
        return Beløp.fra(beløp != null ? BigDecimal.valueOf(beløp) : null);
    }

    public static Beløp fra(Integer beløp) {
        return Beløp.fra(beløp != null ? BigDecimal.valueOf(beløp) : null);
    }

    public static Beløp fra(long beløp) {
        return Beløp.fra(BigDecimal.valueOf(beløp));
    }

    public static Beløp fra(int beløp) {
        return Beløp.fra(BigDecimal.valueOf(beløp));
    }

    public int intValue() {
        return verdi.intValue();
    }

    public long longValue() {
        return verdi.longValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Beløp ob &&
                (Objects.equals(this.verdi(), ob.verdi()) || (this.verdi() != null && ob.verdi() != null && this.compareTo(ob) == 0));

    }

    @Override
    public int hashCode() {
        return Objects.hash(verdi);
    }


    @Override
    public String toString() {
        return verdi != null ? verdi.toString() : null;
    }

    @Override
    public int compareTo(Beløp beløp) {
        return this.verdi.compareTo(beløp.verdi());
    }

}
