package no.nav.folketrygdloven.kalkulator.guitjenester;

/*
 * Mapping mellom kalklulator/modell/typer og kontrakter/felles
 */
public class ModellTyperMapper {
    public static no.nav.folketrygdloven.kalkulator.modell.typer.Beløp beløpFraDto(no.nav.folketrygdloven.kalkulus.felles.v1.Beløp beløp) {
        return no.nav.folketrygdloven.kalkulator.modell.typer.Beløp.fra(beløp != null ? beløp.verdi() : null);
    }

    public static no.nav.folketrygdloven.kalkulus.felles.v1.Beløp beløpTilDto(no.nav.folketrygdloven.kalkulator.modell.typer.Beløp beløp) {
        return no.nav.folketrygdloven.kalkulus.felles.v1.Beløp.fra(beløp != null ? beløp.verdi() : null);
    }

    public static no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad utbetalingsgradFraDto(no.nav.folketrygdloven.kalkulus.felles.v1.Utbetalingsgrad utbetalingsgrad) {
        return utbetalingsgrad == null || utbetalingsgrad.verdi() == null ? null : 
                new no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad(utbetalingsgrad.verdi());
    }
}
