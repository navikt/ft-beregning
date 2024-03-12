package no.nav.folketrygdloven.kalkulator.tid;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;

public class Intervall implements Comparable<Intervall>, Serializable {

    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final LocalDate fomDato;
    private final LocalDate tomDato;

    private Intervall(LocalDate fomDato, LocalDate tomDato) {
        if (fomDato == null) {
            throw new IllegalArgumentException("Fra og med dato må være satt.");
        }
        if (tomDato == null) {
            throw new IllegalArgumentException("Til og med dato må være satt.");
        }
        if (tomDato.isBefore(fomDato)) {
            throw new IllegalArgumentException("Til og med dato før fra og med dato.");
        }
        this.fomDato = fomDato;
        this.tomDato = tomDato;
    }

    public static Intervall fraOgMedTilOgMed(LocalDate fomDato, LocalDate tomDato) {
        return new Intervall(fomDato, tomDato);
    }

    public static Intervall fraOgMed(LocalDate fomDato) {
        return new Intervall(fomDato, TIDENES_ENDE);
    }

    public static <V> Intervall fraSegment(LocalDateSegment<V> segment) {
        return new Intervall(segment.getFom(), segment.getTom());
    }

    public static Intervall fra(LocalDateInterval localDateInterval) {
        return new Intervall(localDateInterval.getFomDato(), localDateInterval.getTomDato());
    }



    public LocalDate getFomDato() {
        return fomDato;
    }

    public LocalDate getTomDato() {
        return tomDato;
    }

    public boolean overlapper(Intervall other) {
        boolean fomBeforeOrEqual = this.getFomDato().isBefore(other.getTomDato()) || this.getFomDato().isEqual(other.getTomDato());
        boolean tomAfterOrEqual = this.getTomDato().isAfter(other.getFomDato()) || this.getTomDato().isEqual(other.getFomDato());
        boolean overlapper = fomBeforeOrEqual && tomAfterOrEqual;
        return overlapper;
    }

    public boolean inkluderer(Intervall other) {
        boolean fomBeforeOrEqualFom = this.getFomDato().isBefore(other.getFomDato()) || this.getFomDato().isEqual(other.getFomDato());
        boolean tomAfterOrEqualTom = this.getTomDato().isAfter(other.getTomDato()) || this.getTomDato().isEqual(other.getTomDato());
        return fomBeforeOrEqualFom && tomAfterOrEqualTom;
    }

    public boolean erHelg() {
        var starterLørdag = fomDato.getDayOfWeek().equals(DayOfWeek.SATURDAY);
        var slutterLørdag = tomDato.getDayOfWeek().equals(DayOfWeek.SATURDAY);

        var starterSøndag = fomDato.getDayOfWeek().equals(DayOfWeek.SUNDAY);
        var slutterSøndag = tomDato.getDayOfWeek().equals(DayOfWeek.SUNDAY);
        var antallDager = fomDato.datesUntil(tomDato.plusDays(1)).count();
        return (starterLørdag || starterSøndag) && (slutterLørdag || slutterSøndag) &&
                antallDager <= 2;
    }

    public boolean inkluderer(LocalDate dato) {
        Objects.requireNonNull(dato, "null dato, periode=" + this);
        return erEtterEllerLikPeriodestart(dato) && erFørEllerLikPeriodeslutt(dato);
    }

    private boolean erEtterEllerLikPeriodestart(LocalDate dato) {
        Objects.requireNonNull(dato, "null dato, periode=" + this);
        return (getFomDato().isBefore(dato) || getFomDato().isEqual(dato));
    }

    private boolean erFørEllerLikPeriodeslutt(LocalDate dato) {
        Objects.requireNonNull(dato, "null dato, periode=" + this);
        return (getTomDato() == null || getTomDato().isAfter(dato) || getTomDato().isEqual(dato));
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Intervall)) {
            return false;
        }
        Intervall annen = (Intervall) object;
        return Objects.equals(this.getFomDato(), annen.getFomDato())
                && Objects.equals(this.getTomDato(), annen.getTomDato());
    }

    @Override
    public int compareTo(Intervall periode) {
        return getFomDato().compareTo(periode.getFomDato());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFomDato(), getTomDato());
    }

    @Override
    public String toString() {
        return String.format("Periode: %s - %s", getFomDato().format(FORMATTER), getTomDato().format(FORMATTER));
    }
}
