package no.nav.folketrygdloven.kalkulus.felles.v1;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record Beløp(@JsonValue
                    @Valid @NotNull
                    @DecimalMin(value = "-10000000000.00")
                    @DecimalMax(value = "1000000000.00")
                    @Digits(integer = 10, fraction = 2)
                    BigDecimal verdi) implements Comparable<Beløp> {

    public static final Beløp ZERO = Beløp.fra(BigDecimal.ZERO);

    public Beløp {
        Objects.requireNonNull(verdi);
    }

    public static Beløp fra(BigDecimal beløp) {
        return beløp != null ? new Beløp(beløp) : null;
    }

    /*
     * Trengs pga persisterte tilfelle av serialisert gammel BeløpDto - klasse med felt verdi - kan være tomt objekt
     * Vil som regel komme inn som Integer (123) eller Double (123.45) eller objekt (Map). Resten for sikkerhets skyld
     */
    @SuppressWarnings("rawtypes")
    @JsonCreator
    public static Beløp fraGenerell(Object beløp) {
        if (beløp == null) {
            return null;
        }
        var asBigDecimal = switch (beløp) {
            case Integer i -> new BigDecimal(i);
            case Double d -> BigDecimal.valueOf(d);
            case Map map -> !map.isEmpty() && map.get("verdi") != null ? new BigDecimal(String.valueOf(map.get("verdi"))) : null;
            case BigDecimal bd -> bd;
            case Number n -> new BigDecimal(n.toString());
            case String s -> new BigDecimal(s);
            default -> throw new IllegalArgumentException("Støtter ikke node av type: " + beløp.getClass());
        };
        return Beløp.fra(asBigDecimal);
    }

    public static Beløp fra(int beløp) {
        return Beløp.fra(BigDecimal.valueOf(beløp));
    }

    public static BigDecimal safeVerdi(Beløp beløp) {
        return beløp == null ? null : beløp.verdi();
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
