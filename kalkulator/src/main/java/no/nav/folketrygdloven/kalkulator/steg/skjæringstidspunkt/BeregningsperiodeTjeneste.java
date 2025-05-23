package no.nav.folketrygdloven.kalkulator.steg.skjæringstidspunkt;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class BeregningsperiodeTjeneste {

    public Intervall fastsettBeregningsperiodeForATFLAndeler(LocalDate skjæringstidspunkt) {
        var fom = skjæringstidspunkt.minusMonths(3).withDayOfMonth(1);
        var tom = skjæringstidspunkt.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        return Intervall.fraOgMedTilOgMed(fom, tom);
    }


}
