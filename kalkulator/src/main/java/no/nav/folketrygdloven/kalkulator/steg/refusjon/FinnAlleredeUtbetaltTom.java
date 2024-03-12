package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

/**
 * Utbetalingsdato i NAV varierer, vi bruker den 18. i hver måned da dette som oftest vil være rett,
 * men ikke i alle måneder.
 */
public final class FinnAlleredeUtbetaltTom {
    private FinnAlleredeUtbetaltTom() {
        // skjul public constructor
    }
    static Optional<LocalDate> finn(BeregningsgrunnlagDto originaltGrunnlag) {
        var sisteDagMedDagsatsOpt = originaltGrunnlag.getBeregningsgrunnlagPerioder()
                .stream()
                .filter(p -> p.getDagsats() != null && p.getDagsats() > 0)
                .map(BeregningsgrunnlagPeriodeDto::getPeriode)
                .map(Intervall::getTomDato)
                .max(Comparator.naturalOrder());
        var sisteMuligeDag = finnSisteMuligeUtbetalingsdag();
        return sisteDagMedDagsatsOpt.map(sisteDagMedDagsats -> sisteDagMedDagsats.isBefore(sisteMuligeDag) ? sisteDagMedDagsats : sisteMuligeDag);
    }

    private static LocalDate finnSisteMuligeUtbetalingsdag() {
        LocalDate idag = LocalDate.now();
        int utbetalingsdagIMåned = finnUtbetalingsdagForMåned(idag.getMonth());
        if (idag.getDayOfMonth() > utbetalingsdagIMåned) {
            return idag.with(TemporalAdjusters.lastDayOfMonth());
        } else {
            return idag.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        }
    }

    private static int finnUtbetalingsdagForMåned(Month month) {
        // Desember utbetaling er alltid tidligere enn andre måneder, spesialbehandles.
        if (month == Month.DECEMBER) {
            return 7;
        }
        return 18;
    }

}
