package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagModell.Kravperioder;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

/**
 * Normaliserer refusjonskravet fra en inntektsmelding til sammenhengende, ikke-overlappende
 * kravperioder innenfor de gyldige utbetalingsperiodene.
 *
 * <p>Normaliseringen gjør følgende:
 * <ul>
 *     <li>Endringer som faller før startdato slås sammen til å gjelde fra startdato.</li>
 *     <li>Endringer som faller i et hull mellom gyldige perioder flyttes til starten av neste gyldige periode.</li>
 *     <li>Eventuelt opphørsdato nuller ut kravet fra dagen etter opphør.</li>
 *     <li>Beløp på eller under {@value #MINSTE_UTBETALTE_REFUSJONSKRAV_PR_MÅNED} nullstilles,
 *     fordi Nav ikke utbetaler refusjon under denne terskelen.</li>
 * </ul>
 */
final class NormaliserRefusjonsgrunnlag {

    private static final BigDecimal MINSTE_UTBETALTE_REFUSJONSKRAV_PR_MÅNED = BigDecimal.valueOf(10.83);

    private NormaliserRefusjonsgrunnlag() {
    }

    static List<Kravperioder> periodiser(InntektsmeldingDto im, LocalDate startdato, List<Intervall> gyldigePerioder) {
        if (gyldigePerioder.isEmpty()) {
            return List.of();
        }
        var endringer = finnEndringer(im, startdato, gyldigePerioder);
        var tidslinje = lagTidslinje(endringer);
        var gyldigTidslinje = new LocalDateTimeline<>(gyldigePerioder.stream()
            .map(p -> LocalDateSegment.emptySegment(p.getFomDato(), p.getTomDato())).toList());
        return tidslinje.intersection(gyldigTidslinje)
            .combine(new LocalDateSegment<>(startdato, TIDENES_ENDE, BigDecimal.ZERO),
                StandardCombinators::sum, LocalDateTimeline.JoinStyle.RIGHT_JOIN)
            .compress().stream()
            .map(s -> new Kravperioder(new Periode(s.getFom(), s.getTom()), s.getValue()))
            .toList();
    }

    private static Map<LocalDate, Beløp> finnEndringer(InntektsmeldingDto im, LocalDate startdato,
                                                     List<Intervall> gyldigePerioder) {
        Map<LocalDate, Beløp> endringer = new TreeMap<>();
        endringer.put(startdato, Optional.ofNullable(im.getRefusjonBeløpPerMnd()).orElse(Beløp.ZERO));
        im.getEndringerRefusjon().stream().sorted(Comparator.comparing(RefusjonDto::getFom))
            .forEach(endring -> {
                var fom = finnEndringsdato(gyldigePerioder, endring.getFom());
                if (fom.isBefore(startdato)) {
                    endringer.put(startdato, endring.getRefusjonsbeløp());
                } else if (fom.isBefore(TIDENES_ENDE)) {
                    endringer.put(fom, endring.getRefusjonsbeløp());
                }
            });
        if (im.getRefusjonOpphører() != null && !TIDENES_ENDE.equals(im.getRefusjonOpphører())) {
            endringer.put(im.getRefusjonOpphører().plusDays(1), Beløp.ZERO);
        }
        return endringer;
    }

    private static LocalDate finnEndringsdato(List<Intervall> gyldigePerioder, LocalDate endringsdato) {
        if (gyldigePerioder.stream().anyMatch(p -> p.inkluderer(endringsdato))) {
            return endringsdato;
        }
        return gyldigePerioder.stream().filter(p -> p.getFomDato().isAfter(endringsdato))
            .map(Intervall::getFomDato).min(Comparator.naturalOrder()).orElse(TIDENES_ENDE);
    }

    private static LocalDateTimeline<BigDecimal> lagTidslinje(Map<LocalDate, Beløp> endringer) {
        var segmenter = endringer.entrySet().stream()
            .map(endring -> {
                var beløp = endring.getValue().verdi();
                return new LocalDateSegment<>(endring.getKey(), TIDENES_ENDE,
                    beløp.compareTo(MINSTE_UTBETALTE_REFUSJONSKRAV_PR_MÅNED) > 0 ? beløp : BigDecimal.ZERO);
            })
            .toList();
        return new LocalDateTimeline<>(segmenter, StandardCombinators::coalesceRightHandSide);
    }
}
