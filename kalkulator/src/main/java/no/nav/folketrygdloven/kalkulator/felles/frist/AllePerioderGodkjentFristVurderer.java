package no.nav.folketrygdloven.kalkulator.felles.frist;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_BEGYNNELSE;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.util.List;

import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class AllePerioderGodkjentFristVurderer {

    public static LocalDateTimeline<Utfall> finnTidslinje() {
        return new LocalDateTimeline<>(List.of(new LocalDateSegment<>(TIDENES_BEGYNNELSE, TIDENES_ENDE, Utfall.GODKJENT)));
    }

}
