package no.nav.folketrygdloven.kalkulator;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonsperiodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class OpprettKravPerioderFraInntektsmeldinger {

    public static List<KravperioderPrArbeidsforholdDto> opprett(InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunktBeregning) {
        List<KravperioderPrArbeidsforholdDto> perioderPrArbeidsgiver = lagKravperioderPrArbeidsforhold(iayGrunnlag, skjæringstidspunktBeregning);
        return perioderPrArbeidsgiver;
    }

    private static List<KravperioderPrArbeidsforholdDto> lagKravperioderPrArbeidsforhold(InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunktBeregning) {
        List<KravperioderPrArbeidsforholdDto> perioderPrArbeidsgiver = new ArrayList<>();
        iayGrunnlag.getInntektsmeldinger()
                .stream()
                .flatMap(im -> im.getAlleInntektsmeldinger().stream())
                .forEach(im -> {
                    PerioderForKravDto perioderForKravDto = lagPerioderForKrav(im, skjæringstidspunktBeregning, skjæringstidspunktBeregning);
                    lagNyEllerLeggTilEksisterende(perioderPrArbeidsgiver, im, perioderForKravDto);
                });
        return perioderPrArbeidsgiver;
    }

    private static void lagNyEllerLeggTilEksisterende(List<KravperioderPrArbeidsforholdDto> perioderPrArbeidsgiver, InntektsmeldingDto im, PerioderForKravDto perioderForKravDto) {
        Optional<KravperioderPrArbeidsforholdDto> eksisterende = perioderPrArbeidsgiver.stream().filter(k -> k.getArbeidsgiver().getIdentifikator().equals(im.getArbeidsgiver().getIdentifikator()) &&
                        k.getArbeidsforholdRef().gjelderFor(im.getArbeidsforholdRef()))
                .findFirst();
        eksisterende.ifPresentOrElse(e -> e.getPerioder().add(perioderForKravDto),
                () -> perioderPrArbeidsgiver.add(new KravperioderPrArbeidsforholdDto(im.getArbeidsgiver(),
                        im.getArbeidsforholdRef(),
                        List.of(perioderForKravDto),
                        perioderForKravDto.getPerioder().stream().map(RefusjonsperiodeDto::periode).collect(Collectors.toList()))));
    }

    private static PerioderForKravDto lagPerioderForKrav(InntektsmeldingDto im, LocalDate innsendingsdato, LocalDate skjæringstidspunktBeregning) {
        PerioderForKravDto perioderForKravDto = new PerioderForKravDto(innsendingsdato, lagPerioder(im, skjæringstidspunktBeregning));
        return perioderForKravDto;
    }

    private static List<RefusjonsperiodeDto> lagPerioder(InntektsmeldingDto im, LocalDate skjæringstidspunktBeregning) {
        ArrayList<LocalDateSegment<Beløp>> alleSegmenter = new ArrayList<>();
        if (!(im.getRefusjonBeløpPerMnd() == null || im.getRefusjonBeløpPerMnd().erNullEller0())) {
            alleSegmenter.add(new LocalDateSegment<>(skjæringstidspunktBeregning,
                    TIDENES_ENDE, im.getRefusjonBeløpPerMnd()));
        }

        alleSegmenter.addAll(im.getEndringerRefusjon().stream().map(e ->
                new LocalDateSegment<>(e.getFom(), TIDENES_ENDE, e.getRefusjonsbeløp())
        ).collect(Collectors.toList()));

        var refusjonTidslinje = new LocalDateTimeline<>(alleSegmenter, (interval, lhs, rhs) -> {
            if (lhs.getFom().isBefore(rhs.getFom())) {
                return new LocalDateSegment<>(interval, rhs.getValue());
            }
            return new LocalDateSegment<>(interval, lhs.getValue());
        });

        return refusjonTidslinje.stream()
                .map(r -> new RefusjonsperiodeDto(Intervall.fraOgMedTilOgMed(r.getFom(), r.getTom()), r.getValue()))
                .collect(Collectors.toList());

    }

    public static List<KravperioderPrArbeidsforholdDto> opprett(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                LocalDate skjæringstidspunktBeregning, Map<Arbeidsgiver, LocalDate> førsteInnsendingsdatoMap) {
        return lagKravperioderPrArbeidsforhold(iayGrunnlag, førsteInnsendingsdatoMap, skjæringstidspunktBeregning);
    }

    private static List<KravperioderPrArbeidsforholdDto> lagKravperioderPrArbeidsforhold(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                         Map<Arbeidsgiver, LocalDate> førsteInnsendingsdatoMap,
                                                                                         LocalDate skjæringstidspunktBeregning) {
        List<KravperioderPrArbeidsforholdDto> perioderPrArbeidsgiver = new ArrayList<>();
        iayGrunnlag.getInntektsmeldinger()
                .stream()
                .flatMap(im -> im.getAlleInntektsmeldinger().stream())
                .forEach(im -> {
                    PerioderForKravDto perioderForKravDto = lagPerioderForKrav(im, førsteInnsendingsdatoMap.get(im.getArbeidsgiver()), skjæringstidspunktBeregning);
                    lagNyEllerLeggTilEksisterende(perioderPrArbeidsgiver, im, perioderForKravDto);
                });
        return perioderPrArbeidsgiver;
    }

}
