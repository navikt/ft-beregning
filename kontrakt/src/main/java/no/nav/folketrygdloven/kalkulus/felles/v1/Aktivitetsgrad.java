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

public record Aktivitetsgrad(@JsonValue
                    @Valid
                    @NotNull
                    @DecimalMin(value = "0.00")
                    @DecimalMax(value = "100.00")
                    @Digits(integer = 3, fraction = 2)
                    BigDecimal verdi) implements Comparable<Aktivitetsgrad> {

    public static final Aktivitetsgrad ZERO = Aktivitetsgrad.fra(BigDecimal.ZERO);
    public static final Aktivitetsgrad HUNDRE = Aktivitetsgrad.fra(BigDecimal.valueOf(100));

    public Aktivitetsgrad {
        Objects.requireNonNull(verdi);
    }

    @JsonCreator
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
