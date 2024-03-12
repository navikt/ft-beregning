package no.nav.folketrygdloven.kalkulator.modell.diff;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Markerer at et felt i en dto skal sjekkes for endringer når det skal kopieres og spoles mellom tilstander
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface SjekkVedKopiering {
}
