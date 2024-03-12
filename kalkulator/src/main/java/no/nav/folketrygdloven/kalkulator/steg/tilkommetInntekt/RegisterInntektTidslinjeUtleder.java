package no.nav.folketrygdloven.kalkulator.steg.tilkommetInntekt;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.inntektgradering.DagsatsPrKategoriOgArbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Virkedager;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

class RegisterInntektTidslinjeUtleder {

    /**
     * Utleder tidslinje for inntekter fra a-ordningen
     *
     * @param fomDato       Første dato i tidslinjen
     * @param inntektposter Alle inntektsposter
     * @param arbeidsgivere Arbeidsgivere som skal vere med i tidslinjen
     * @return Tidslinje for registerinntekt
     */
    public static LocalDateTimeline<Set<DagsatsPrKategoriOgArbeidsgiver>> lagInntektsperioderTidslinje(LocalDate fomDato, Collection<InntektspostDto> inntektposter, List<Arbeidsgiver> arbeidsgivere) {
        var posterPrArbeidsgiver = finnInntektsposterPrRelevantArbeidsgiver(inntektposter, arbeidsgivere);
        var tidslinjer = posterPrArbeidsgiver.entrySet().stream()
                .map(e -> lagInntektTidslinje(fomDato, e.getValue()).mapValue(v -> new DagsatsPrKategoriOgArbeidsgiver(Inntektskategori.ARBEIDSTAKER, e.getKey(), v)))
                .toList();
        return unionAvTidslinjer(tidslinjer);
    }

    private static Map<Arbeidsgiver, List<InntektspostDto>> finnInntektsposterPrRelevantArbeidsgiver(Collection<InntektspostDto> inntektposter, List<Arbeidsgiver> arbeidsgivere) {
        var posterPrArbeidsgiver = inntektposter.stream()
                .filter(p -> p.getInntekt().getArbeidsgiver() != null)
                .collect(Collectors.groupingBy(p -> p.getInntekt().getArbeidsgiver()));
        return arbeidsgivere
                .stream()
                .distinct()
                .collect(Collectors.toMap(Function.identity(), it -> posterPrArbeidsgiver.getOrDefault(it, List.of())));
    }

    private static LocalDateTimeline<Beløp> lagInntektTidslinje(LocalDate fomDato, List<InntektspostDto> inntektposter) {
        List<LocalDateSegment<Beløp>> segmenter = inntektposter.stream().map(p -> new LocalDateSegment<>(
                p.getPeriode().getFomDato(),
                p.getPeriode().getTomDato(),
                regnOmTilDagsats(p))).toList();
        var inntektPerioderTidslinje = new LocalDateTimeline<>(segmenter, RegisterInntektTidslinjeUtleder::summer);
        return fyllMellomromFraDato(fomDato, inntektPerioderTidslinje);
    }

    private static Beløp regnOmTilDagsats(InntektspostDto p) {
        final var antallVirkedager = Optional.of(Virkedager.beregnVirkedager(p.getPeriode().getFomDato(), p.getPeriode().getTomDato()))
                .filter(d -> d != 0)
                .orElseGet(() -> p.getPeriode().getFomDato().until(p.getPeriode().getTomDato().plusDays(1)).getDays());
        return p.getBeløp().divider(antallVirkedager, 2, RoundingMode.HALF_UP);
    }


    private static LocalDateTimeline<Beløp> fyllMellomromFraDato(LocalDate fomDato, LocalDateTimeline<Beløp> inntektPerioderTidslinje) {
        var mellomrom = new LocalDateTimeline<>(new LocalDateInterval(fomDato, LocalDateInterval.TIDENES_ENDE), Beløp.ZERO);
        return inntektPerioderTidslinje.combine(mellomrom, RegisterInntektTidslinjeUtleder::summer, LocalDateTimeline.JoinStyle.CROSS_JOIN);
    }

    public static LocalDateSegment<Beløp> summer(LocalDateInterval dateInterval, LocalDateSegment<Beløp> lhs, LocalDateSegment<Beløp> rhs) {
        return new LocalDateSegment<>(dateInterval, Beløp.safeSum(lhs == null ? null : lhs.getValue(), rhs == null ? null : rhs.getValue()));
    }

    private static LocalDateTimeline<Set<DagsatsPrKategoriOgArbeidsgiver>> unionAvTidslinjer(List<LocalDateTimeline<DagsatsPrKategoriOgArbeidsgiver>> godkjenteTidslinjerPrArbeidsgiver) {
        return godkjenteTidslinjerPrArbeidsgiver.stream()
                .map(t -> t.filterValue(Objects::nonNull).mapValue(Set::of)) // For å kunne bruke union
                .reduce(LocalDateTimeline.empty(), (tl1, tl2) -> tl1.combine(tl2, StandardCombinators::union, LocalDateTimeline.JoinStyle.CROSS_JOIN));
    }

}
