package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

/**
 * Avgjør om refusjonskravet fra inntektsmelding har økt inn i en periode. Bruker rått IM-krav
 * framfor beregningsgrunnlaget, siden {@code refusjonskravPrÅr} der nulles ut ved utbetalingsgrad 0.
 */
final class RefusjonskravFraInntektsmeldingTjeneste {

    private RefusjonskravFraInntektsmeldingTjeneste() {
    }

    /**
     * @return true dersom kravet har økt inn i perioden. Ved manglende kravdata returneres true, slik at
     * et reelt avklaringsbehov ikke undertrykkes.
     */
    static boolean refusjonskravHarØktInnIPeriode(List<KravperioderPrArbeidsforholdDto> kravPrArbeidsgiver,
                                                  Arbeidsgiver arbeidsgiver,
                                                  Intervall periode,
                                                  LocalDate skjæringstidspunkt) {
        if (arbeidsgiver == null || kravPrArbeidsgiver == null || kravPrArbeidsgiver.isEmpty()) {
            return true;
        }
        var perioderForArbeidsgiver = kravPrArbeidsgiver.stream()
                .filter(k -> arbeidsgiver.equals(k.getArbeidsgiver()))
                .flatMap(k -> k.getPerioder().stream())
                .toList();
        if (perioderForArbeidsgiver.isEmpty()) {
            return true;
        }
        var effektivKravTidslinje = byggKravbeløpTidslinje(perioderForArbeidsgiver);
        if (effektivKravTidslinje.isEmpty()) {
            return true;
        }
        var periodeIntervall = new LocalDateInterval(periode.getFomDato(), periode.getTomDato());

        // Kravet i perioden er høyere enn kravet før perioden
        var kravIPeriode = maksBeløp(effektivKravTidslinje.intersection(periodeIntervall));
        var maksKravFør = maksKravFørPeriode(effektivKravTidslinje, periode, skjæringstidspunkt);
        if (kravIPeriode.compareTo(maksKravFør) > 0) {
            return true;
        }

        // En nyere inntektsmelding har hevet kravet i perioden
        return nyereInntektsmeldingHarHevetKravIPeriode(perioderForArbeidsgiver, periodeIntervall);
    }

    private static Beløp maksKravFørPeriode(LocalDateTimeline<Beløp> kravTidslinje, Intervall periode, LocalDate skjæringstidspunkt) {
        if (!periode.getFomDato().isAfter(skjæringstidspunkt)) {
            return Beløp.ZERO;
        }
        var førPeriode = new LocalDateInterval(skjæringstidspunkt, periode.getFomDato().minusDays(1));
        return maksBeløp(kravTidslinje.intersection(førPeriode));
    }

    private static boolean nyereInntektsmeldingHarHevetKravIPeriode(List<PerioderForKravDto> perioderForArbeidsgiver, LocalDateInterval periodeIntervall) {
        var nyesteInnsendingsdato = perioderForArbeidsgiver.stream()
                .map(PerioderForKravDto::getInnsendingsdato)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());
        if (nyesteInnsendingsdato.isEmpty()) {
            return false;
        }
        var eldreKrav = perioderForArbeidsgiver.stream()
                .filter(k -> k.getInnsendingsdato() == null || k.getInnsendingsdato().isBefore(nyesteInnsendingsdato.get()))
                .toList();
        if (eldreKrav.isEmpty()) {
            // Ingen tidligere innsending å sammenligne mot
            return false;
        }
        var effektivIPeriode = byggKravbeløpTidslinje(perioderForArbeidsgiver).intersection(periodeIntervall);
        var eldreIPeriode = byggKravbeløpTidslinje(eldreKrav).intersection(periodeIntervall);
        var økningTidslinje = effektivIPeriode.combine(eldreIPeriode,
                (intervall, effektiv, eldre) -> new LocalDateSegment<>(intervall,
                        verdiEllerNull(effektiv).compareTo(verdiEllerNull(eldre)) > 0),
                LocalDateTimeline.JoinStyle.CROSS_JOIN);
        return økningTidslinje.stream().anyMatch(s -> Boolean.TRUE.equals(s.getValue()));
    }

    private static Beløp verdiEllerNull(LocalDateSegment<Beløp> segment) {
        return segment == null || segment.getValue() == null ? Beløp.ZERO : segment.getValue();
    }

    private static LocalDateTimeline<Beløp> byggKravbeløpTidslinje(List<PerioderForKravDto> perioder) {
        var kravSortertEldstFørst = perioder.stream()
                .sorted(Comparator.comparing(PerioderForKravDto::getInnsendingsdato, Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();
        LocalDateTimeline<Beløp> tidslinje = LocalDateTimeline.empty();
        for (var krav : kravSortertEldstFørst) {
            var segmenter = krav.getPerioder().stream()
                    .map(p -> new LocalDateSegment<>(p.periode().getFomDato(), p.periode().getTomDato(), p.beløp() == null ? Beløp.ZERO : p.beløp()))
                    .toList();
            // Nyeste inntektsmelding vinner ved overlapp
            tidslinje = tidslinje.combine(new LocalDateTimeline<>(segmenter), StandardCombinators::coalesceRightHandSide, LocalDateTimeline.JoinStyle.CROSS_JOIN);
        }
        return tidslinje.compress();
    }

    private static Beløp maksBeløp(LocalDateTimeline<Beløp> tidslinje) {
        return tidslinje.stream()
                .map(LocalDateSegment::getValue)
                .filter(Objects::nonNull)
                .max(Beløp::compareTo)
                .orElse(Beløp.ZERO);
    }
}
