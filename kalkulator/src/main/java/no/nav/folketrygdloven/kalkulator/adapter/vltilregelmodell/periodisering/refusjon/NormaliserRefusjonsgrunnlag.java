package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.RefusjonsgrunnlagModell.Kravperioder;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektsmeldingType;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

final class NormaliserRefusjonsgrunnlag {

    private static final BigDecimal MINSTE_UTBETALTE_REFUSJONSKRAV_PR_MÅNED = BigDecimal.valueOf(10.83);

    private NormaliserRefusjonsgrunnlag() {
    }

    static List<Kravperioder> periodiser(InntektsmeldingDto im, LocalDate startdato,
                                       Optional<BeregningRefusjonOverstyringerDto> overstyringer,
                                       List<Intervall> gyldigePerioder) {
        if (gyldigePerioder.isEmpty()) {
            return List.of();
        }
        var endringer = finnEndringer(im, startdato, overstyringer, gyldigePerioder);
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
                                                     Optional<BeregningRefusjonOverstyringerDto> overstyringer,
                                                     List<Intervall> gyldigePerioder) {
        var overstyrtStartdato = overstyringer.map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer)
            .orElse(List.of()).stream()
            .filter(o -> o.getArbeidsgiver().equals(im.getArbeidsgiver()))
            .findFirst()
            .map(BeregningRefusjonOverstyringDto::getRefusjonPerioder).orElse(List.of()).stream()
            .filter(p -> Objects.equals(p.getArbeidsforholdRef().getReferanse(), im.getArbeidsforholdRef().getReferanse()))
            .findFirst().map(BeregningRefusjonPeriodeDto::getStartdatoRefusjon);
        var gjeldendeStartdato = overstyrtStartdato.orElseGet(() ->
            im.getInntektsmeldingType().equals(InntektsmeldingType.REFUSJONSKRAV)
                ? im.getStartDatoPermisjon().filter(dato -> dato.isAfter(startdato)).orElse(startdato)
                : startdato);
        Map<LocalDate, Beløp> endringer = new TreeMap<>();
        endringer.put(gjeldendeStartdato, Optional.ofNullable(im.getRefusjonBeløpPerMnd()).orElse(Beløp.ZERO));
        im.getEndringerRefusjon().stream().sorted(Comparator.comparing(RefusjonDto::getFom))
            .forEach(endring -> {
                var fom = finnEndringsdato(gyldigePerioder, endring.getFom());
                if (fom.isBefore(gjeldendeStartdato)) {
                    endringer.put(gjeldendeStartdato, endring.getRefusjonsbeløp());
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
        var liste = new ArrayList<>(endringer.entrySet());
        List<LocalDateSegment<BigDecimal>> segmenter = new ArrayList<>();
        for (int i = 0; i < liste.size(); i++) {
            var endring = liste.get(i);
            var tom = i + 1 < liste.size() ? liste.get(i + 1).getKey().minusDays(1) : TIDENES_ENDE;
            var beløp = endring.getValue().verdi();
            segmenter.add(new LocalDateSegment<>(endring.getKey(), tom,
                beløp.compareTo(MINSTE_UTBETALTE_REFUSJONSKRAV_PR_MÅNED) > 0 ? beløp : BigDecimal.ZERO));
        }
        return new LocalDateTimeline<>(segmenter);
    }
}
