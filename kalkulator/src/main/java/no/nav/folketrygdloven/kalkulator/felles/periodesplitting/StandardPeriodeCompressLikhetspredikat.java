package no.nav.folketrygdloven.kalkulator.felles.periodesplitting;

import java.util.Objects;

public class StandardPeriodeCompressLikhetspredikat {

    private StandardPeriodeCompressLikhetspredikat() {
        // Skjuler default konstruktør
    }

    public static <V> boolean komprimerNårLike(V a, V b) {
        return Objects.equals(a, b);
    }


    public static <V> boolean aldriKomprimer(V ignoredA, V ignoredB) {
        return false;
    }

}
