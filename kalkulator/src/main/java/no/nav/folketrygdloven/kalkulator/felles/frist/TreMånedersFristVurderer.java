package no.nav.folketrygdloven.kalkulator.felles.frist;

import static no.nav.folketrygdloven.kalkulator.felles.frist.StartRefusjonTjeneste.finnFørsteGyldigeDatoMedRefusjon;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class TreMånedersFristVurderer {

    public static LocalDateTimeline<Utfall> finnTidslinje(PerioderForKravDto krav, Optional<LocalDate> overstyrtRefusjonFom) {
        return new LocalDateTimeline<>(List.of(new LocalDateSegment<>(
                overstyrtRefusjonFom.orElse(finnFørsteGyldigeDatoMedRefusjon(krav.getInnsendingsdato())),
                TIDENES_ENDE, Utfall.GODKJENT)));
    }

}
