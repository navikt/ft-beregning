package no.nav.folketrygdloven.kalkulator.tid;

import static java.lang.Math.toIntExact;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;


public class Virkedager {

    private static final int DAGER_PR_UKE = 7;
    private static final int VIRKEDAGER_PR_UKE = 5;
    private static final int HELGEDAGER_PR_UKE = DAGER_PR_UKE - VIRKEDAGER_PR_UKE;

    private Virkedager() {
        // For å unngå instanser
    }

    public static int beregnAntallVirkedagerEllerKunHelg(LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(fom);
        Objects.requireNonNull(tom);
        if (fom.isAfter(tom)) {
            throw new IllegalArgumentException("Utviklerfeil: fom " + fom + " kan ikke være før tom " + tom);
        }

        int varighetDager = (int) (tom.toEpochDay() - fom.toEpochDay() + 1);
        if (varighetDager <= 2 && erHelg(fom) && erHelg(tom)) {
            return varighetDager;
        }

        return beregnVirkedager(fom, tom);
    }

    public static int beregnVirkedager(LocalDate fom, LocalDate tom) {
        try {
            // Utvid til nærmeste mandag tilbake i tid fra og med begynnelse (fom) (0-6 dager)
            int padBefore = fom.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            // Utvid til nærmeste søndag fram i tid fra og med slutt (tom) (0-6 dager)
            int padAfter = DayOfWeek.SUNDAY.getValue() - tom.getDayOfWeek().getValue();
            // Antall virkedager i perioden utvidet til hele uker
            int virkedagerPadded = toIntExact(ChronoUnit.WEEKS.between(fom.minusDays(padBefore), tom.plusDays(padAfter).plusDays(1)) * VIRKEDAGER_PR_UKE);
            // Antall virkedager i utvidelse
            int virkedagerPadding = Math.min(padBefore, VIRKEDAGER_PR_UKE) + Math.max(padAfter - HELGEDAGER_PR_UKE, 0);
            // Virkedager i perioden uten virkedagene fra utvidelse
            return virkedagerPadded - virkedagerPadding;
        } catch (ArithmeticException e) {
            throw new UnsupportedOperationException("Perioden er for lang til å beregne virkedager.", e);
        }
    }

    private static boolean erHelg(LocalDate dato) {
        return dato.getDayOfWeek().equals(DayOfWeek.SATURDAY) || dato.getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }

}
