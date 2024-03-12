package no.nav.folketrygdloven.kalkulator.ytelse.fp;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon.MapRefusjonPerioderFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class MapRefusjonPerioderFraVLTilRegelFP extends MapRefusjonPerioderFraVLTilRegel {

    public MapRefusjonPerioderFraVLTilRegelFP() {
        super();
    }

    @Override
    protected List<Intervall> finnGyldigeRefusjonPerioder(LocalDate startdatoPermisjon, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag, InntektsmeldingDto inntektsmelding, List<AktivitetsAvtaleDto> alleAnsattperioderForInntektsmeldingEtterStartAvBeregning, Set<YrkesaktivitetDto> yrkesaktiviteter) {
        if (inntektsmelding.getRefusjonOpphører() != null && inntektsmelding.getRefusjonOpphører().isBefore(startdatoPermisjon)) {
            // Refusjon opphører før det utledede startpunktet, blir aldri refusjon
            return Collections.emptyList();
        }
        var utbetalingTidslinje = new LocalDateTimeline<>(List.of(new LocalDateSegment<>(startdatoPermisjon, TIDENES_ENDE, Boolean.TRUE)));
        return utbetalingTidslinje.getLocalDateIntervals()
                .stream()
                .map(it -> Intervall.fraOgMedTilOgMed(it.getFomDato(), it.getTomDato()))
                .toList();
    }

    // For CDI (for håndtere at annotation propageres til subklasser)

}
