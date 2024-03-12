package no.nav.folketrygdloven.kalkulator.modell.diff;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Ignorer alltid et felt for forskjeller ved traversing av dto graph. (se TraverseEntityGraph).*/
@Target(FIELD)
@Retention(RUNTIME)
public @interface DiffIgnore {

}
