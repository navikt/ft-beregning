package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulus.iay.IayProsent;

/**
 * Stillingsprosent slik det er oppgitt i arbeidsavtalen
 */
public record Stillingsprosent(BigDecimal verdi) implements Comparable<Stillingsprosent> {

    public static final Stillingsprosent ZERO = Stillingsprosent.fra(BigDecimal.ZERO);
    public static final Stillingsprosent HUNDRED = Stillingsprosent.fra(100);

    public Stillingsprosent {
        Objects.requireNonNull(verdi);
        if (verdi.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Negativ prosent");
        }
    }

    public static Stillingsprosent fra(BigDecimal grad) {
        return grad != null ? new Stillingsprosent(grad) : null;
    }

    public static Stillingsprosent fra(Integer grad) {
        return grad != null ? new Stillingsprosent(BigDecimal.valueOf(grad)) : null;
    }

    public static Stillingsprosent fra(IayProsent grad) {
        return Optional.ofNullable(grad).map(IayProsent::verdi).map(Stillingsprosent::fra).orElse(null);
    }

    public boolean erNullEller0() {
        return verdi == null || this.compareTo(ZERO) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Stillingsprosent ob && (Objects.equals(this.verdi(), ob.verdi()) || (this.verdi() != null && ob.verdi() != null && this.compareTo(ob) == 0));

    }

    @Override
    public int hashCode() {
        return Objects.hash(verdi);
    }

    @Override
    public int compareTo(Stillingsprosent beløp) {
        return this.verdi.compareTo(beløp.verdi());
    }

}
