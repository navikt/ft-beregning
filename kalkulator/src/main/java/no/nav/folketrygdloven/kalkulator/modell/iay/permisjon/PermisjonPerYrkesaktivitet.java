package no.nav.folketrygdloven.kalkulator.modell.iay.permisjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PermisjonsbeskrivelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public final class PermisjonPerYrkesaktivitet {

    public static LocalDateTimeline<Boolean> utledPermisjonPerYrkesaktivitet(YrkesaktivitetDto yrkesaktivitet,
                                                                             Map<YtelseType, LocalDateTimeline<Boolean>> tidslinjePerYtelse, LocalDate skjæringstidspunkt) {
        List<LocalDateTimeline<Boolean>> aktivPermisjonTidslinjer = yrkesaktivitet.getPermisjoner()
                .stream()
                .filter(permisjon -> erStørreEllerLik100Prosent(permisjon.getProsentsats()))
                .map(it -> justerPeriodeEtterYtelse(it, tidslinjePerYtelse, skjæringstidspunkt))
                .flatMap(Collection::stream)
                .map(permisjon -> new LocalDateTimeline<>(permisjon.getFomDato(), permisjon.getTomDato(), Boolean.TRUE))
                .toList();
        LocalDateTimeline<Boolean> aktivPermisjonTidslinje = new LocalDateTimeline<>(List.of());
        for (LocalDateTimeline<Boolean> linje : aktivPermisjonTidslinjer) {
            aktivPermisjonTidslinje = aktivPermisjonTidslinje.combine(linje, StandardCombinators::coalesceRightHandSide, LocalDateTimeline.JoinStyle.CROSS_JOIN);
        }
        return aktivPermisjonTidslinje;
    }

    private static Set<Intervall> justerPeriodeEtterYtelse(PermisjonDto it,
                                                           Map<YtelseType, LocalDateTimeline<Boolean>> tidslinjePerYtelse,
                                                           LocalDate skjæringstidspunkt) {
        var vilkårsperiodeTidslinje = new LocalDateTimeline<>(List.of(new LocalDateSegment<>(skjæringstidspunkt, TIDENES_ENDE, true)));
        if (Objects.equals(it.getPermisjonsbeskrivelseType(), PermisjonsbeskrivelseType.PERMISJON_MED_FORELDREPENGER)) {
            var foreldrepengerTidslinje = tidslinjePerYtelse.getOrDefault(YtelseType.FORELDREPENGER, new LocalDateTimeline<>(List.of()));
            var svangerskapspengerTidslinje = tidslinjePerYtelse.getOrDefault(YtelseType.SVANGERSKAPSPENGER, new LocalDateTimeline<>(List.of()));

            var permisjonstidslinje = new LocalDateTimeline<>(List.of(new LocalDateSegment<>(it.getPeriode().getFomDato(), it.getPeriode().getTomDato(), true)));
            permisjonstidslinje = permisjonstidslinje.disjoint(foreldrepengerTidslinje).disjoint(svangerskapspengerTidslinje).disjoint(vilkårsperiodeTidslinje);

            return permisjonstidslinje.compress()
                    .toSegments()
                    .stream()
                    .map(LocalDateSegment::getLocalDateInterval)
                    .map(p -> Intervall.fraOgMedTilOgMed(p.getFomDato(), p.getTomDato()))
                    .collect(Collectors.toCollection(TreeSet::new));
        } else if (it.getPermisjonsbeskrivelseType() != null && PermisjonsbeskrivelseType.K9_VELFERDSPERMISJON.contains(it.getPermisjonsbeskrivelseType())) {
            var permisjonstidslinje = new LocalDateTimeline<>(List.of(new LocalDateSegment<>(it.getPeriode().getFomDato(), it.getPeriode().getTomDato(), true)));
            for (var aktivitetType : FagsakYtelseType.K9_YTELSER) {
                var ytelsesTidslinje = tidslinjePerYtelse.getOrDefault(aktivitetType, new LocalDateTimeline<>(List.of()));
                permisjonstidslinje = permisjonstidslinje.disjoint(ytelsesTidslinje);
            }
            return permisjonstidslinje.compress()
                    .toSegments()
                    .stream()
                    .map(LocalDateSegment::getLocalDateInterval)
                    .map(p -> Intervall.fraOgMedTilOgMed(p.getFomDato(), p.getTomDato()))
                    .collect(Collectors.toCollection(TreeSet::new));
        }
        return Set.of(Intervall.fraOgMedTilOgMed(it.getPeriode().getFomDato(), it.getPeriode().getTomDato()));
    }

    private static boolean erStørreEllerLik100Prosent(Stillingsprosent prosentsats) {
        return prosentsats.compareTo(Stillingsprosent.HUNDRED) >= 0;
    }
}
