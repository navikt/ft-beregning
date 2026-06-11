package no.nav.folketrygdloven.kalkulator.guitjenester;

import no.nav.folketrygdloven.kalkulus.felles.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.Utbetalingsgrad;

/*
 * Mapping mellom kalklulator/modell/typer og kontrakter/felles
 */
public class ModellTyperMapper {

    private ModellTyperMapper() {
        // Skjuler default konstruktør
    }

    public static no.nav.folketrygdloven.kalkulator.modell.typer.Beløp beløpFraDto(Beløp beløp) {
        return no.nav.folketrygdloven.kalkulator.modell.typer.Beløp.fra(beløp != null ? beløp.verdi() : null);
    }

    public static Beløp beløpTilDto(no.nav.folketrygdloven.kalkulator.modell.typer.Beløp beløp) {
        return Beløp.fra(beløp != null ? beløp.verdi() : null);
    }

    public static no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad utbetalingsgradFraDto(Utbetalingsgrad utbetalingsgrad) {
        return utbetalingsgrad == null || utbetalingsgrad.verdi() == null ? null :
                new no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad(utbetalingsgrad.verdi());
    }
}
