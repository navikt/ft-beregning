package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.io.Serializable;
import java.util.Objects;

/**
 * Ekstern arbeidsforhold referanse.
 * Mottatt fra inntektsmelding eller AARegisteret.
 * <p>
 * Hvis null gjelder det flere arbeidsforhold, ellers for et spesifikt forhold
 */

public class EksternArbeidsforholdRef implements Serializable {

    /**
     * Representerer alle arbeidsforhold for en arbeidsgiver.
     */
    private static final EksternArbeidsforholdRef NULL_OBJECT = new EksternArbeidsforholdRef(null);

    private String referanse;

    private EksternArbeidsforholdRef(String referanse) {
        this.referanse = referanse;
    }

    public static EksternArbeidsforholdRef ref(String referanse) {
        return new EksternArbeidsforholdRef(referanse);
    }

    public static EksternArbeidsforholdRef nullRef() {
        return NULL_OBJECT;
    }

    public String getReferanse() {
        return referanse;
    }

    public boolean gjelderForSpesifiktArbeidsforhold() {
        return referanse != null && !referanse.isEmpty();
    }

    public boolean gjelderFor(EksternArbeidsforholdRef ref) {
        Objects.requireNonNull(ref, "Forventer EksternArbeidsforholdRef.ref(null)");
        if (!gjelderForSpesifiktArbeidsforhold() || !ref.gjelderForSpesifiktArbeidsforhold()) {
            return true;
        }
        return Objects.equals(referanse, ref.referanse);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null && this.referanse == null) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) return false;
        EksternArbeidsforholdRef that = (EksternArbeidsforholdRef) o;
        return Objects.equals(referanse, that.referanse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referanse);
    }

    @Override
    public String toString() {
        return referanse;
    }
}
