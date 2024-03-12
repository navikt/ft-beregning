package no.nav.folketrygdloven.kalkulator.modell.iay;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public interface OppgittPeriodeInntekt {

    Intervall getPeriode();

    Beløp getInntekt();

}
