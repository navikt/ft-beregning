package no.nav.folketrygdloven.kalkulator.modell.iay;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class OppgittFrilansInntektDto implements OppgittPeriodeInntekt {

    private Intervall periode;
    private Beløp inntekt;

    public OppgittFrilansInntektDto(Intervall periode, Beløp inntekt) {
        this.periode = periode;
        this.inntekt = inntekt;
    }

    @Override
    public Intervall getPeriode() {
        return periode;
    }

    @Override
    public Beløp getInntekt() {
        return inntekt;
    }

}
