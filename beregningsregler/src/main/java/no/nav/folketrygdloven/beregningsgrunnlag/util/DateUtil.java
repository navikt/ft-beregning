package no.nav.folketrygdloven.beregningsgrunnlag.util;

import java.time.LocalDate;
import java.time.Month;

/**
 * Returner funksjonelt tidsoffset, Brukes med LocalDate og LocalDateTime. eks. LocalDate.now(FPDateUtil.getOffset)
 */
public class DateUtil {

	private DateUtil() {
		// Skjuler default konstruktør
	}

    /** Null object pattern - for å unngå håndtere null (men må av og til håndtere denne). */
    public static final LocalDate TIDENES_BEGYNNELSE = LocalDate.of(-4712, Month.JANUARY, 1);

    /** Null object pattern - for å unngå håndtere null (men må av og til håndtere denne). */
    public static final LocalDate TIDENES_ENDE = LocalDate.of(9999, Month.DECEMBER, 31);

}
