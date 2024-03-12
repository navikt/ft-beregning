package no.nav.folketrygdloven.kalkulator.felles.periodesplitting;

import java.util.List;
import java.util.function.BiFunction;

import no.nav.folketrygdloven.kalkulator.avklaringsbehov.PerioderTilVurderingTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class StandardPeriodeSplittMappers {

    public static <V> BiFunction<LocalDateTimeline<BeregningsgrunnlagPeriodeDto>,LocalDateTimeline<V>,  LocalDateTimeline<BeregningsgrunnlagPeriodeDto>> settAvsluttetPeriodeårsak(List<Intervall> forlengelseperioder, PeriodeÅrsak avsluttetPeriodeårsak) {

        return (tidslinje, nyePerioderTidslinje) -> settAvsluttetPeriodeårsakMapper(nyePerioderTidslinje, forlengelseperioder, avsluttetPeriodeårsak, tidslinje);
    }

    private static <V> LocalDateTimeline<BeregningsgrunnlagPeriodeDto> settAvsluttetPeriodeårsakMapper(LocalDateTimeline<V> nyePerioderTidslinje,
                                                                                                       List<Intervall> forlengelseperioder,
                                                                                                       PeriodeÅrsak avsluttetPeriodeårsak,
                                                                                                       LocalDateTimeline<BeregningsgrunnlagPeriodeDto> tidslinje) {
        var perioderTilVurderingTjeneste = new PerioderTilVurderingTjeneste(forlengelseperioder, tidslinje.toSegments().stream().map(s -> Intervall.fraOgMedTilOgMed(s.getFom(), s.getTom())).toList());
        var gamleperioder = tidslinje.disjoint(nyePerioderTidslinje);
        gamleperioder.stream()
                .filter(perioderTilVurderingTjeneste::erTilVurdering)
                .filter(s -> nyePerioderTidslinje.stream().anyMatch(it -> it.getTom().equals(s.getFom().minusDays(1))))
                .forEach(p -> BeregningsgrunnlagPeriodeDto.oppdater(p.getValue()).leggTilPeriodeÅrsak(avsluttetPeriodeårsak));
        return tidslinje;
    }

}
