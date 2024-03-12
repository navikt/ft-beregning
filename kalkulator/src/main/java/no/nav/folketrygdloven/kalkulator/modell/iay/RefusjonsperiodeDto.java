package no.nav.folketrygdloven.kalkulator.modell.iay;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public record RefusjonsperiodeDto (Intervall periode, Beløp beløp) { }
