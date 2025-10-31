package no.nav.folketrygdloven.kalkulator;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonsperiodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class OpprettKravPerioderFraInntektsmeldingerForTest {

    public static List<KravperioderPrArbeidsforholdDto> opprett(InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunktBeregning) {
        return lagKravperioderPrArbeidsforhold(iayGrunnlag, skjæringstidspunktBeregning);
    }

    private static List<KravperioderPrArbeidsforholdDto> lagKravperioderPrArbeidsforhold(InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunktBeregning) {
        var agImMap = getArbeidsgiverImMap(iayGrunnlag);
        return agImMap.entrySet().stream()
            .map(entry -> {
                var refusjonPerioder = entry.getValue()
                    .stream()
                    .map(im -> lagPerioderForKrav(im, skjæringstidspunktBeregning, skjæringstidspunktBeregning))
                    .toList();
                var perioder = refusjonPerioder.stream()
                    .map(PerioderForKravDto::getPerioder)
                    .flatMap(Collection::stream)
                    .map(RefusjonsperiodeDto::periode)
                    .toList();
                return new KravperioderPrArbeidsforholdDto(entry.getKey(), refusjonPerioder, perioder);
            }).toList();
    }

    private static Map<Arbeidsgiver, List<InntektsmeldingDto>> getArbeidsgiverImMap(InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return iayGrunnlag.getInntektsmeldinger()
            .map(InntektsmeldingAggregatDto::getAlleInntektsmeldinger)
            .orElse(List.of())
            .stream()
            .collect(Collectors.groupingBy(InntektsmeldingDto::getArbeidsgiver));
    }

    private static PerioderForKravDto lagPerioderForKrav(InntektsmeldingDto im, LocalDate innsendingsdato, LocalDate skjæringstidspunktBeregning) {
        return new PerioderForKravDto(innsendingsdato, lagPerioder(im, skjæringstidspunktBeregning));
    }

    private static List<RefusjonsperiodeDto> lagPerioder(InntektsmeldingDto im, LocalDate skjæringstidspunktBeregning) {
        var alleSegmenter = new ArrayList<LocalDateSegment<Beløp>>();
        if (!(im.getRefusjonBeløpPerMnd() == null || im.getRefusjonBeløpPerMnd().erNullEller0())) {
            alleSegmenter.add(new LocalDateSegment<>(skjæringstidspunktBeregning,
                    TIDENES_ENDE, im.getRefusjonBeløpPerMnd()));
        }

        alleSegmenter.addAll(im.getEndringerRefusjon().stream().map(e ->
                new LocalDateSegment<>(e.getFom(), TIDENES_ENDE, e.getRefusjonsbeløp())
        ).toList());

        var refusjonTidslinje = new LocalDateTimeline<>(alleSegmenter, (interval, lhs, rhs) -> {
            if (lhs.getFom().isBefore(rhs.getFom())) {
                return new LocalDateSegment<>(interval, rhs.getValue());
            }
            return new LocalDateSegment<>(interval, lhs.getValue());
        });

        return refusjonTidslinje.stream()
                .map(r -> new RefusjonsperiodeDto(Intervall.fraOgMedTilOgMed(r.getFom(), r.getTom()), r.getValue()))
                .toList();

    }

    public static List<KravperioderPrArbeidsforholdDto> opprett(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                LocalDate skjæringstidspunktBeregning, Map<Arbeidsgiver, LocalDate> førsteInnsendingsdatoMap) {
        var arbeidsgiverImMap = getArbeidsgiverImMap(iayGrunnlag);
        return arbeidsgiverImMap.entrySet().stream()
            .map(entry -> {
                var refusjonPerioder = entry.getValue()
                    .stream()
                    .map(im -> lagPerioderForKrav(im, førsteInnsendingsdatoMap.get(entry.getKey()), skjæringstidspunktBeregning))
                    .toList();
                var perioder = refusjonPerioder.stream().map(PerioderForKravDto::getPerioder).flatMap(Collection::stream).toList();
                return new KravperioderPrArbeidsforholdDto(entry.getKey(), refusjonPerioder, perioder.stream().map(RefusjonsperiodeDto::periode).toList());
            }).toList();
    }
}
