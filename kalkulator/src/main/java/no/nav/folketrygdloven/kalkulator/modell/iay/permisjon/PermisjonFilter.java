package no.nav.folketrygdloven.kalkulator.modell.iay.permisjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_BEGYNNELSE;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.tid.TimelineWeekendCompressor;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;


public class PermisjonFilter {

    private final Map<YtelseType, LocalDateTimeline<Boolean>> tidslinjePerYtelse;
    private final Collection<YrkesaktivitetDto> yrkesaktiviteter;
    private final LocalDate skjæringstidspunkt;

    public PermisjonFilter(Collection<YtelseDto> ytelser,
                           Collection<YrkesaktivitetDto> yrkesaktiviteter, LocalDate skjæringstidspunkt) {
        this.skjæringstidspunkt = skjæringstidspunkt;
        var ytelseperioder = ytelser.stream().map(Ytelseperiode::new).toList();
        this.tidslinjePerYtelse = utledYtelsesTidslinjer(ytelseperioder);
        this.yrkesaktiviteter = yrkesaktiviteter;
    }


    public LocalDateTimeline<Boolean> tidslinjeForPermisjoner(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        var relevantYrkesaktivitet = yrkesaktiviteter
                .stream()
                .filter(ya -> ya.gjelderFor(
                        arbeidsgiver,
                        arbeidsforholdRef))
                .findFirst();
        return relevantYrkesaktivitet.map(this::finnTidslinjeForPermisjonOver14Dager)
                .orElse(LocalDateTimeline.empty());
    }

    public LocalDateTimeline<Boolean> finnTidslinjeForPermisjonOver14Dager(YrkesaktivitetDto yrkesaktivitet) {

        // Permisjoner på yrkesaktivitet
        LocalDateTimeline<Boolean> aktivPermisjonTidslinje = PermisjonPerYrkesaktivitet.utledPermisjonPerYrkesaktivitet(yrkesaktivitet, tidslinjePerYtelse, skjæringstidspunkt);

        // Vurder kun permisjonsperioder over aktivitetens lengde og fra gitt dato
        LocalDateTimeline<Boolean> tidslinjeTilVurdering = new LocalDateTimeline<>(List.of(new LocalDateSegment<>(TIDENES_BEGYNNELSE, TIDENES_ENDE, Boolean.TRUE)));
        tidslinjeTilVurdering = tidslinjeTilVurdering.intersection(aktivPermisjonTidslinje.compress());
        var aktivitetsTidslinje = new LocalDateTimeline<>(yrkesaktivitet.getAlleAnsettelsesperioder().stream()
                .map(AktivitetsAvtaleDto::getPeriode)
                .map(p -> new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), Boolean.TRUE))
                .toList(), StandardCombinators::alwaysTrueForMatch);
        tidslinjeTilVurdering = tidslinjeTilVurdering.intersection(aktivitetsTidslinje.compress());

        // Legg til mellomliggende periode dersom helg mellom permisjonsperioder
        tidslinjeTilVurdering = komprimerForHelg(tidslinjeTilVurdering);

        // Underkjent vurderingsstatus dersom sammenhengende permisjonsperiode > 14 dager
        var permisjonOver14Dager = tidslinjeTilVurdering.compress().stream()
                .filter(segment -> segment.getValue() == Boolean.TRUE && segment.getLocalDateInterval().days() > 14)
                .collect(Collectors.toSet());

        return new LocalDateTimeline<>(permisjonOver14Dager);
    }

    private Map<YtelseType, LocalDateTimeline<Boolean>> utledYtelsesTidslinjer(Collection<Ytelseperiode> aktiviteter) {
        var gruppertPåYtelse = aktiviteter.stream()
                .collect(Collectors.groupingBy(Ytelseperiode::ytelseType));
        var timelinePerYtelse = new HashMap<YtelseType, LocalDateTimeline<Boolean>>();

        for (var entry : gruppertPåYtelse.entrySet()) {
            var segmenter = entry.getValue()
                    .stream()
                    .map(it -> new LocalDateSegment<>(it.periode().getFomDato(), it.periode().getTomDato(), true))
                    .collect(Collectors.toSet());
            var timeline = new LocalDateTimeline<>(segmenter, StandardCombinators::alwaysTrueForMatch);
            timeline = komprimerForHelg(timeline);
            timelinePerYtelse.put(entry.getKey(), timeline);
        }

        return timelinePerYtelse;
    }

    private static LocalDateTimeline<Boolean> komprimerForHelg(LocalDateTimeline<Boolean> tidslinje) {
        var factory = new TimelineWeekendCompressor.CompressorFactory<Boolean>(Objects::equals, (i, lhs, rhs) -> new LocalDateSegment<>(i, lhs.getValue()));
        TimelineWeekendCompressor<Boolean> compressor = tidslinje.toSegments().stream()
                .collect(factory::get, TimelineWeekendCompressor::accept, TimelineWeekendCompressor::combine);
        return new LocalDateTimeline<>(compressor.getSegmenter());
    }

    private record Ytelseperiode(YtelseType ytelseType, Intervall periode) {

        private Ytelseperiode(YtelseDto ytelse) {
            this(ytelse.getYtelseType(), ytelse.getPeriode());
        }

    }

}
