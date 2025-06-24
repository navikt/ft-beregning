package no.nav.folketrygdloven.kalkulus.felles.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Egen verditype for bruk i kontrakter der vi eksplisitt ikke ønsker å validere beløpet (hovedsakelig når det kommer fra register)
 * @param verdi
 */
public record BeløpIkkeValidert(@JsonValue
                    @Valid @NotNull
                    BigDecimal verdi) implements Comparable<BeløpIkkeValidert> {

    public static final BeløpIkkeValidert ZERO = BeløpIkkeValidert.fra(BigDecimal.ZERO);

    public BeløpIkkeValidert {
        Objects.requireNonNull(verdi);
    }

    public static BeløpIkkeValidert fra(BigDecimal beløp) {
        return beløp != null ? new BeløpIkkeValidert(beløp) : null;
    }

    /*
     * Trengs pga persisterte tilfelle av serialisert gammel BeløpDto - klasse med felt verdi - kan være tomt objekt
     * Vil som regel komme inn som Integer (123) eller Double (123.45) eller objekt (Map). Resten for sikkerhets skyld
     */
    @SuppressWarnings("rawtypes")
    @JsonCreator
    public static BeløpIkkeValidert fraGenerell(Object beløp) {
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
        return BeløpIkkeValidert.fra(asBigDecimal);
    }

    public static BeløpIkkeValidert fra(int beløp) {
        return BeløpIkkeValidert.fra(BigDecimal.valueOf(beløp));
    }

    public static BigDecimal safeVerdi(BeløpIkkeValidert beløp) {
        return beløp == null ? null : beløp.verdi();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof BeløpIkkeValidert ob &&
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
    public int compareTo(BeløpIkkeValidert beløp) {
        return this.verdi.compareTo(beløp.verdi());
    }

}
