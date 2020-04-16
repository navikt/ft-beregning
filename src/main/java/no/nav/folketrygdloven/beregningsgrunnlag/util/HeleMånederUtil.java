package no.nav.folketrygdloven.beregningsgrunnlag.util;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

/**
 * UTIL klasse som brukes for utledning av beregningsperiode for FRISINN
 */
public final class HeleMånederUtil {

    private HeleMånederUtil() {
        // Vedskjul
    }
    /**
     *
     * @param dato1 sluttdato for første periode
     * @param dato2 startdato for neste periode
     * @return hvor mange hele måneder det er mellom disse datoene
     */
    public static int heleMånederMellom(LocalDate dato1, LocalDate dato2) {
        Objects.requireNonNull(dato1, "dato1");
        Objects.requireNonNull(dato2, "dato2");
        LocalDate startdato = dato1.isAfter(dato2) ? dato2 : dato1;
        LocalDate sluttdato = dato1.isAfter(dato2) ? dato1 : dato2;
        int counter = 0;
        if (startdato.equals(sluttdato)) {
            return counter;
        }
        if (startdato.getDayOfMonth() == 1) {
            startdato = startdato.minusDays(1);
        }
        while(startdato.isBefore(sluttdato)) {
            if (!startdato.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).isBefore(sluttdato)) {
                break;
            }
            startdato = startdato.plusMonths(1).withDayOfMonth(1);
            counter++;
        }
        return counter;
    }

}
