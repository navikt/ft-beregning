package no.nav.folketrygdloven.kalkulator.felles;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.tid.Virkedager;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public class MeldekortUtils {

    // Gammel konvensjon fra Arena der full utbetaling i 2 uker er 200%
    public static final BigDecimal ARENA_DIVISOR = BigDecimal.valueOf(2);

    private record UtbetalingMedNormertUtbetalingsprosent(YtelseAnvistDto utbetaling, boolean fullUtbetaling, Stillingsprosent normertUtbetalingsprosent) {}

    private MeldekortUtils() {}

    public static Optional<YtelseDto> sisteVedtakFørStpForType(YtelseFilterDto ytelseFilter, LocalDate skjæringstidspunkt, Set<YtelseType> ytelseTyper) {
        return ytelseFilter.getFiltrertYtelser().stream()
            .filter(ytelse -> ytelseTyper.contains(ytelse.getYtelseType()))
            .filter(ytelse -> !skjæringstidspunkt.isBefore(ytelse.getPeriode().getFomDato()))
            .max(Comparator.comparing(YtelseDto::getPeriode).thenComparing(ytelse -> ytelse.getPeriode().getTomDato()));
    }

    public static Optional<Beløp> sisteAnvisteDagsatsFørStpForType(YtelseFilterDto ytelseFilter, LocalDate skjæringstidspunkt, Set<YtelseType> ytelseTyper) {
        return ytelseFilter.getFiltrertYtelser().stream()
            .filter(ytelse -> ytelseTyper.contains(ytelse.getYtelseType()))
            .map(YtelseDto::getYtelseAnvist)
            .flatMap(Collection::stream)
            .filter(a -> !skjæringstidspunkt.isBefore(a.getAnvistFOM()))
            .max(Comparator.comparing(YtelseAnvistDto::getAnvistFOM))
            .flatMap(YtelseAnvistDto::getDagsats);
    }

    public static LocalDateTimeline<BigDecimal> utbetalingsgradForIntervallYtelse(YtelseFilterDto ytelseFilter, LocalDateInterval intervall, Set<YtelseType> ytelseTyper) {
        return ytelseFilter.getFiltrertYtelser().stream()
            .filter(ytelse -> ytelseTyper.contains(ytelse.getYtelseType()))
            .flatMap(ytelse -> getUtbetalingsGrad(ytelse, intervall))
            .collect(Collectors.collectingAndThen(Collectors.toList(), l -> new LocalDateTimeline<>(l, StandardCombinators::max)));
    }

    private static Stream<LocalDateSegment<BigDecimal>> getUtbetalingsGrad(YtelseDto ytelse, LocalDateInterval intervall) {
        return ytelse.getYtelseAnvist().stream()
            .filter(a -> Intervall.fra(intervall).overlapper(a.getAnvistPeriode()))
            .map(a -> new LocalDateSegment<>(a.getAnvistPeriode().getFomDato(), a.getAnvistPeriode().getTomDato(), getUtbetalingsGrad(ytelse, a)));
    }

    public static Optional<BigDecimal> snittUtbetalingsgradSistePeriodeFørStp(YtelseFilterDto ytelseFilter, YtelseDto sisteVedtak,
                                                                              LocalDate skjæringstidspunkt, Set<YtelseType> ytelseTyper) {
        return utbetalingsgradSistePeriodeFørStp(ytelseFilter, sisteVedtak, skjæringstidspunkt, ytelseTyper)
            .map(LocalDateSegment::getValue);
    }

    public static Optional<LocalDateSegment<BigDecimal>> utbetalingsgradSistePeriodeFørStp(YtelseFilterDto ytelseFilter, YtelseDto sisteVedtak,
                                                                                           LocalDate skjæringstidspunkt, Set<YtelseType> ytelseTyper) {
        if (YtelseKilde.ARENA.equals(sisteVedtak.getYtelseKilde())) {
            // For ytelse med kilde ARENA er alle anviste perioder akkurat 2 uker
            var sisteAnvisningFørStp = sisteHeleMeldekortFørStp(ytelseFilter, sisteVedtak, skjæringstidspunkt, ytelseTyper);
            return sisteAnvisningFørStp
                .map(m -> new LocalDateSegment<>(m.utbetaling().getAnvistFOM(), m.utbetaling().getAnvistTOM(),
                    m.normertUtbetalingsprosent().tilNormalisertGrad()));
        } else {
            // Andre kilder vil ha anviste perioder med varighet fra 1 dag til 1 år
            final var intervallFom = sisteVedtak.getPeriode().getFomDato().minusWeeks(5);
            var utbetalingsgradTidslinje = utbetalingsgradForIntervallYtelse(ytelseFilter, new LocalDateInterval(intervallFom, skjæringstidspunkt), ytelseTyper);
            var stpBaseline = skjæringstidspunkt.minusWeeks(1).with(DayOfWeek.SUNDAY);
            var sisteTom = utbetalingsgradTidslinje.isEmpty() ? stpBaseline : Optional.of(utbetalingsgradTidslinje.getMaxLocalDate())
                .filter(tom -> tom.isBefore(stpBaseline))
                .orElse(stpBaseline);
            var periodeForSnitt = new LocalDateInterval(sisteTom.minusDays(13), sisteTom);
            var tidslinjeForSnitt = new LocalDateTimeline<>(periodeForSnitt, BigDecimal.ZERO);
            var virkedagerForPeriode = BigDecimal.valueOf(Virkedager.beregnVirkedager(sisteTom.minusDays(13), sisteTom));
            var summertgrad = tidslinjeForSnitt.combine(utbetalingsgradTidslinje, StandardCombinators::max, LocalDateTimeline.JoinStyle.LEFT_JOIN).compress().stream()
                .reduce(BigDecimal.ZERO, (acc, seg) -> acc.add(ubetalingsgradForPeriode(seg)), BigDecimal::add);
            var gjennomsnittUtbetalingsgrad = summertgrad.divide(virkedagerForPeriode, 10, RoundingMode.HALF_EVEN);
            return Optional.of(new LocalDateSegment<>(periodeForSnitt, gjennomsnittUtbetalingsgrad));
        }
    }

    private static BigDecimal ubetalingsgradForPeriode(LocalDateSegment<BigDecimal> seg) {
        return seg.getValue().multiply(BigDecimal.valueOf(Virkedager.beregnVirkedager(seg.getFom(), seg.getTom())));
    }

    private static Optional<UtbetalingMedNormertUtbetalingsprosent> sisteHeleMeldekortFørStp(YtelseFilterDto ytelseFilter, YtelseDto sisteVedtak,
                                                                                            LocalDate skjæringstidspunkt, Set<YtelseType> ytelseTyper) {
        // For ytelse med kilde ARENA er alle anviste perioder akkurat 2 uker
        final var sisteVedtakFom = sisteVedtak.getPeriode().getFomDato();

        var alleMeldekort = finnAlleMeldekort(ytelseFilter, ytelseTyper);

        var sisteMeldekortOpt = alleMeldekort.stream()
            .filter(u -> sisteVedtakFom.minus(KonfigTjeneste.getMeldekortPeriode()).isBefore(u.utbetaling().getAnvistTOM()))
            .filter(u -> skjæringstidspunkt.isAfter(u.utbetaling().getAnvistTOM()))
            .max(Comparator.comparing(u -> u.utbetaling().getAnvistFOM()));

        if (sisteMeldekortOpt.isEmpty()) {
            return Optional.empty();
        }

        var sisteMeldekort = sisteMeldekortOpt.get();

        // Vi er nødt til å sjekke om vi har flere meldekort med samme periode
        // TODO: Er dette virkelig nødvendig? Har vi noen tilfelle i praksis? Det lar seg sjekke.
        var alleMeldekortMedPeriode = alleMeldekortMedPeriode(sisteMeldekort, alleMeldekort);
        if (alleMeldekortMedPeriode.size() > 1) {
            return finnMeldekortSomGjelderForVedtak(alleMeldekortMedPeriode, sisteVedtak);
        }

        return Optional.of(sisteMeldekort);
    }

    public static boolean finnesMeldekortSomInkludererGittDato(YtelseFilterDto ytelseFilter, Set<YtelseType> ytelseTyper, LocalDate gittDato) {
        return finnAlleMeldekort(ytelseFilter, ytelseTyper).stream()
            .anyMatch(ytelseAnvist -> ytelseAnvist.utbetaling().getAnvistPeriode().inkluderer(gittDato));
    }

    public static BigDecimal getUtbetalingsGrad(YtelseDto ytelse, YtelseAnvistDto anvist) {
        return mapTilNormertUtbetalingsprosent(ytelse, anvist).normertUtbetalingsprosent().tilNormalisertGrad();
    }

    private static List<UtbetalingMedNormertUtbetalingsprosent> finnAlleMeldekort(YtelseFilterDto ytelseFilter, Set<YtelseType> ytelseTyper){
        return ytelseFilter.getFiltrertYtelser().stream()
            .filter(ytelse -> ytelseTyper.contains(ytelse.getYtelseType()))
            .flatMap(ytelse -> ytelse.getYtelseAnvist().stream().map(u -> mapTilNormertUtbetalingsprosent(ytelse, u)))
            .toList();
    }

    private static UtbetalingMedNormertUtbetalingsprosent mapTilNormertUtbetalingsprosent(YtelseDto ytelse, YtelseAnvistDto utbetaling) {
        // Etterhvert så må denne dele på 2 kun dersom kilde == ARENA - default er 0..100%
        var normert = utbetaling.getUtbetalingsgradProsent()
            .map(Stillingsprosent::verdi)
            .map(up -> ytelse.harKildeKelvinEllerDpSak() ? up : up.divide(ARENA_DIVISOR, up.scale() + 1, RoundingMode.HALF_UP))
            .map(Stillingsprosent::fra)
            .orElse(Stillingsprosent.HUNDRED);
        return new UtbetalingMedNormertUtbetalingsprosent(utbetaling, Stillingsprosent.HUNDRED.compareTo(normert) == 0, normert);
    }


    private static List<UtbetalingMedNormertUtbetalingsprosent> alleMeldekortMedPeriode(UtbetalingMedNormertUtbetalingsprosent siste,
                                                                                        List<UtbetalingMedNormertUtbetalingsprosent> alleMeldekort) {
        return alleMeldekort.stream()
            .filter(meldekort -> Objects.equals(meldekort.utbetaling().getAnvistFOM(), siste.utbetaling().getAnvistFOM()))
            .filter(meldekort -> Objects.equals(meldekort.utbetaling().getAnvistTOM(), siste.utbetaling().getAnvistTOM()))
            .toList();
    }

    private static Optional<UtbetalingMedNormertUtbetalingsprosent> finnMeldekortSomGjelderForVedtak(List<UtbetalingMedNormertUtbetalingsprosent> meldekort, YtelseDto sisteVedtak) {
        return meldekort.stream().filter(m -> matcherMeldekortFraSisteVedtak(m.utbetaling(), sisteVedtak)).findFirst();
    }

    private static boolean matcherMeldekortFraSisteVedtak(YtelseAnvistDto meldekort, YtelseDto sisteVedtak) {
        return sisteVedtak.getYtelseAnvist().stream().anyMatch(ya -> Objects.equals(ya, meldekort));
    }

}
