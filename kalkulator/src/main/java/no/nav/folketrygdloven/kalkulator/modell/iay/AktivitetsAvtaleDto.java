package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class AktivitetsAvtaleDto {

    private boolean erAnsettelsesPeriode = true;
    private Intervall periode;
    private LocalDate sisteLønnsendringsdato;

    /**
     * Setter en periode brukt til overstyring av angitt periode (avledet fra saksbehandlers vurderinger). Benyttes kun transient (ved filtrering av modellen)
     */
    private Intervall overstyrtPeriode;

    AktivitetsAvtaleDto() {
        // hibernate
    }

    /**
     * Deep copy ctor
     */
    AktivitetsAvtaleDto(AktivitetsAvtaleDto aktivitetsAvtale) {
        this.sisteLønnsendringsdato = aktivitetsAvtale.getSisteLønnsendringsdato();
        this.periode = aktivitetsAvtale.getPeriodeUtenOverstyring();
        this.erAnsettelsesPeriode = aktivitetsAvtale.erAnsettelsesPeriode();
    }

    public AktivitetsAvtaleDto(AktivitetsAvtaleDto avtale, Intervall overstyrtPeriode) {
        this(avtale);
        this.overstyrtPeriode = overstyrtPeriode;
    }

    /**
     * Perioden til aktivitetsavtalen.
     * Tar hensyn til overstyring gjort i 5080.
     *
     * @return Hele perioden, tar hensyn til overstyringer.
     */
    public Intervall getPeriode() {
        return erOverstyrtPeriode() ? overstyrtPeriode : periode;
    }

    /**
     * Henter kun den originale perioden, ikke den overstyrte perioden.
     * Bruk heller {@link #getPeriode} i de fleste tilfeller
     * @return Hele den originale perioden, uten overstyringer.
     */

    Intervall getPeriodeUtenOverstyring() {
        return periode;
    }

    void setPeriode(Intervall periode) {
        this.periode = periode;
    }

    void setErAnsettelsesPeriode(boolean erAnsettelsesPeriode) {
        this.erAnsettelsesPeriode = erAnsettelsesPeriode;
    }

    /**
     * Hvorvidet denne avtalen har en overstyrt periode.
     */

    private boolean erOverstyrtPeriode() {
        return overstyrtPeriode != null;
    }


    public LocalDate getSisteLønnsendringsdato() {
        return sisteLønnsendringsdato;
    }


    boolean matcherPeriode(Intervall aktivitetsAvtale) {
        return getPeriode().equals(aktivitetsAvtale);
    }

    void sisteLønnsendringsdato(LocalDate sisteLønnsendringsdato) {
        this.sisteLønnsendringsdato = sisteLønnsendringsdato;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof AktivitetsAvtaleDto)) return false;
        AktivitetsAvtaleDto that = (AktivitetsAvtaleDto) o;
        return Objects.equals(periode, that.periode) &&
            Objects.equals(sisteLønnsendringsdato, that.sisteLønnsendringsdato) &&
            Objects.equals(erAnsettelsesPeriode, that.erAnsettelsesPeriode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, sisteLønnsendringsdato, erAnsettelsesPeriode);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
            ", periode=" + periode + //$NON-NLS-1$
            ", overstyrtPeriode=" + overstyrtPeriode + //$NON-NLS-1$
            ", sisteLønnsendringsdato="+sisteLønnsendringsdato + //$NON-NLS-1$
            ", erAnsettelsesPeriode="+erAnsettelsesPeriode + //$NON-NLS-1$
            '>';
    }

    boolean hasValues() {
        return periode != null;
    }


    public boolean erAnsettelsesPeriode() {
        return erAnsettelsesPeriode;
    }

}
