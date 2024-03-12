package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.UUID;

import no.nav.folketrygdloven.kalkulator.modell.diff.IndexKey;
import no.nav.folketrygdloven.kalkulus.kodeverk.KodeKonstanter;

/**
 * Intern arbeidsforhold referanse.
 * <p>
 * Hvis null gjelder det flere arbeidsforhold, ellers for et spesifikt forhold
 */

public class InternArbeidsforholdRefDto implements Serializable, IndexKey {

    /**
     * Instans som representerer alle arbeidsforhold (for en arbeidsgiver).
     */
    private static final InternArbeidsforholdRefDto NULL_OBJECT = new InternArbeidsforholdRefDto(null);

    private UUID referanse;

    private InternArbeidsforholdRefDto(UUID referanse) {
        this.referanse = referanse;
    }

    public static InternArbeidsforholdRefDto ref(UUID referanse) {
        return referanse == null ? NULL_OBJECT : new InternArbeidsforholdRefDto(referanse);
    }

    public static InternArbeidsforholdRefDto ref(String referanse) {
        return referanse == null ? NULL_OBJECT : new InternArbeidsforholdRefDto(UUID.fromString(referanse));
    }

    public static InternArbeidsforholdRefDto nullRef() {
        return NULL_OBJECT;
    }

    public static InternArbeidsforholdRefDto nyRef() {
        return ref(UUID.randomUUID().toString());
    }

    /**
     * Genererer en UUID type 3 basert på angitt seed. Gir konsekvente UUIDer
     */
    public static InternArbeidsforholdRefDto namedRef(String seed) {
        return ref(UUID.nameUUIDFromBytes(seed.getBytes(Charset.forName("UTF8"))).toString());
    }

    public String getReferanse() {
        return referanse == null ? null : referanse.toString();
    }

    public UUID getUUIDReferanse() {
        return referanse;
    }

    public boolean gjelderForSpesifiktArbeidsforhold() {
        return referanse != null;
    }

    public boolean gjelderFor(InternArbeidsforholdRefDto ref) {
        Objects.requireNonNull(ref, "Forventer InternArbeidsforholdRef.nullRef()");
        if (!gjelderForSpesifiktArbeidsforhold() || !ref.gjelderForSpesifiktArbeidsforhold()) {
            return true;
        }
        return Objects.equals(referanse, ref.referanse);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null && this.referanse == null) {
            return true;
        }
        if (o == null || getClass() != o.getClass())
            return false;
        InternArbeidsforholdRefDto that = (InternArbeidsforholdRefDto) o;
        return Objects.equals(referanse, that.referanse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referanse);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + (referanse == null ? "" : referanse.toString()) + ">";
    }

    @Override
    public String getIndexKey() {
        return referanse != null ? getReferanse() : KodeKonstanter.UDEFINERT;
    }
}
