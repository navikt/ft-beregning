package no.nav.folketrygdloven.kalkulator.felles;

import java.time.LocalDate;

/**
 * Tjeneste for å finne siste aktivitetsdag/ beregningstidspunkt.
 *
 */
public class BeregningstidspunktTjeneste {

    private BeregningstidspunktTjeneste() {
        // Skjul konstruktør
    }


    /**
     * Finne datogrensen for inkluderte aktiviteter/beregningstidspunkt. Aktiviteter som slutter på eller etter denne datoen blir med i beregningen.
     *
     * @param skjæringstidspunkt skjæringstidspunkt
     * @return Dato for inkluderte aktiviteter
     */
    // Vi må vurdere om dette skal fortsette å vere ein static klasse eller om vi burde lage eit interface med implementasjoner pr ytelse
    public static LocalDate finnBeregningstidspunkt(LocalDate skjæringstidspunkt) {
        return skjæringstidspunkt.minusDays(1);
    }

}
