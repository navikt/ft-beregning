package no.nav.folketrygdloven.kalkulator.felles.frist;

import static no.nav.folketrygdloven.kalkulator.felles.frist.StartRefusjonTjeneste.finnFørsteMuligeDagRefusjon;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class KravTjeneste {

    private KravTjeneste() {
    }
    /** Finner tidslinje for refusjonskrav og vurdering av om det er mottatt innen fristen
     *
     * @param kravperioder Perioder for refusjonskrav
     * @param yrkesaktivitet Yrkesaktivitet
     * @param gjeldendeAktiviteter  Alle aktiviteter som er benyttet i beregning
     * @param skjæringstidspunktBeregning   Skjæringstidspunkte
     * @param overstyrtGodkjentRefusjonFom  Overstyrt fom-dato for når refusjonskravet er ansett som godkjent
     * @param ytelseType
     * @return Tidslinje for mottatt krav og utfall av fristvurdering
     */
    public static LocalDateTimeline<KravOgUtfall> lagTidslinjeForYrkesaktivitet(List<PerioderForKravDto> kravperioder,
                                                                                YrkesaktivitetDto yrkesaktivitet,
                                                                                BeregningAktivitetAggregatDto gjeldendeAktiviteter,
                                                                                LocalDate skjæringstidspunktBeregning,
                                                                                Optional<LocalDate> overstyrtGodkjentRefusjonFom,
                                                                                FagsakYtelseType ytelseType) {
        return kravperioder.stream()
                .map(krav -> finnFristvurdertTidslinje(krav, yrkesaktivitet, gjeldendeAktiviteter, skjæringstidspunktBeregning, overstyrtGodkjentRefusjonFom, ytelseType))
                .reduce(KombinerRefusjonskravFristTidslinje::kombinerOgKompress)
                .orElse(new LocalDateTimeline<>(Collections.emptyList()));
    }

    private static LocalDateTimeline<KravOgUtfall> finnFristvurdertTidslinje(PerioderForKravDto krav,
                                                                      YrkesaktivitetDto yrkesaktivitet,
                                                                      BeregningAktivitetAggregatDto gjeldendeAktiviteter,
                                                                      LocalDate skjæringstidspunktBeregning,
                                                                      Optional<LocalDate> overstyrtGodkjentRefusjonFom,
                                                                      FagsakYtelseType ytelseType) {
        var kravTidslinje = finnKravTidslinje(krav, yrkesaktivitet, gjeldendeAktiviteter, skjæringstidspunktBeregning);
        var godkjentTidslinje = finnGodkjentTidslinje(krav, overstyrtGodkjentRefusjonFom, ytelseType);
        return kravTidslinje.combine(godkjentTidslinje, (intervall, lhs, rhs) -> {
            if (rhs == null) {
                return new LocalDateSegment<>(intervall, new KravOgUtfall(lhs.getValue(), Utfall.UNDERKJENT));
            }
            return new LocalDateSegment<>(intervall, new KravOgUtfall(lhs.getValue(), rhs.getValue()));
        }, LocalDateTimeline.JoinStyle.LEFT_JOIN);
    }

    private static LocalDateTimeline<Utfall> finnGodkjentTidslinje(PerioderForKravDto krav, Optional<LocalDate> overstyrtGodkjentRefusjonFom, FagsakYtelseType ytelseType) {
        return switch (ytelseType) {
            case FORELDREPENGER, SVANGERSKAPSPENGER -> TreMånedersFristVurderer.finnTidslinje(krav, overstyrtGodkjentRefusjonFom);
            case PLEIEPENGER_SYKT_BARN, OPPLÆRINGSPENGER, OMSORGSPENGER, PLEIEPENGER_NÆRSTÅENDE -> AllePerioderGodkjentFristVurderer.finnTidslinje();
            case UDEFINERT, FRISINN -> throw new IllegalStateException("Utviklerfeil: Fant ingen fristvurderer for ytelsetype=" + ytelseType);
        };
    }

    private static LocalDateTimeline<Beløp> finnKravTidslinje(PerioderForKravDto krav,
                                                              YrkesaktivitetDto yrkesaktivitet,
                                                              BeregningAktivitetAggregatDto gjeldendeAktiviteter,
                                                              LocalDate skjæringstidspunktBeregning) {
        LocalDate førsteMuligeRefusjonsdato = finnFørsteMuligeDagRefusjon(gjeldendeAktiviteter, skjæringstidspunktBeregning, yrkesaktivitet);
        return krav.getPerioder().stream()
                .filter(p -> !p.periode().getTomDato().isBefore(førsteMuligeRefusjonsdato))
                .map(p -> new LocalDateSegment<>(
                        p.periode().inkluderer(førsteMuligeRefusjonsdato) ? førsteMuligeRefusjonsdato : p.periode().getFomDato(),
                        p.periode().getTomDato(),
                        p.beløp()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), LocalDateTimeline::new));
    }

}
