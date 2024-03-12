package no.nav.folketrygdloven.utils;

import java.util.Objects;

public class Tuple<X, Y> {
    private final X element1;
    private final Y element2;

    public static <X, Y> Tuple<X, Y> of(X element1, Y element2) {
        return new Tuple(element1, element2);
    }

    public Tuple(X element1, Y element2) {
        Objects.requireNonNull(element1);
        Objects.requireNonNull(element2);
        this.element1 = element1;
        this.element2 = element2;
    }

    public X getElement1() {
        return this.element1;
    }

    public Y getElement2() {
        return this.element2;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Tuple)) {
            return false;
        } else {
            Tuple<?, ?> tuple = (Tuple)o;
            return this.element1.equals(tuple.element1) && this.element2.equals(tuple.element2);
        }
    }

    public int hashCode() {
        int result = this.element1.hashCode();
        result = 31 * result + this.element2.hashCode();
        return result;
    }
}
