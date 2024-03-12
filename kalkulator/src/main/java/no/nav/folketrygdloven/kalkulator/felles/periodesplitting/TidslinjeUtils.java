package no.nav.folketrygdloven.kalkulator.felles.periodesplitting;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class TidslinjeUtils {

    public static LocalDateTimeline<Boolean> opprettTidslinje(List<Intervall> perioder) {
        return new LocalDateTimeline<>(perioder.stream().map(p -> new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), Boolean.TRUE)).toList());
    }


}
