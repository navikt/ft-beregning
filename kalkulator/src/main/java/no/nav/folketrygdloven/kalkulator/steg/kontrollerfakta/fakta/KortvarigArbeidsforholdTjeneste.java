package no.nav.folketrygdloven.kalkulator.steg.kontrollerfakta.fakta;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class KortvarigArbeidsforholdTjeneste {

    private KortvarigArbeidsforholdTjeneste() {
        // Skjul
    }

    public static boolean harKortvarigeArbeidsforholdOgErIkkeSN(BeregningsgrunnlagDto beregningsgrunnlag, InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        if (brukerHarStatusSN(beregningsgrunnlag)) {
            return false;
        }
        return !hentAndelerForKortvarigeArbeidsforhold(beregningsgrunnlag, inntektArbeidYtelseGrunnlag).isEmpty();
    }

    public static Map<BeregningsgrunnlagPrStatusOgAndelDto, YrkesaktivitetDto> hentAndelerForKortvarigeArbeidsforhold(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                                                      InntektArbeidYtelseGrunnlagDto iayGrunnlag) {

        List<BeregningsgrunnlagPeriodeDto> beregningsgrunnlagPerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();
        if (!beregningsgrunnlagPerioder.isEmpty()) {
            // beregningsgrunnlagPerioder er sortert, tar utgangspunkt i første
            BeregningsgrunnlagPeriodeDto beregningsgrunnlagPeriode = beregningsgrunnlagPerioder.get(0);
            Collection<YrkesaktivitetDto> kortvarigeArbeidsforhold = hentKortvarigeYrkesaktiviteter(beregningsgrunnlag, iayGrunnlag);

            return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                    .filter(prStatus -> prStatus.getAktivitetStatus().equals(AktivitetStatus.ARBEIDSTAKER))
                    .filter(andel -> finnKorresponderendeYrkesaktivitet(
                            kortvarigeArbeidsforhold,
                            andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver),
                            andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)).isPresent())
                    .collect(Collectors.toMap(Function.identity(),
                            andel -> finnKorresponderendeYrkesaktivitet(
                                    kortvarigeArbeidsforhold,
                                    andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver),
                                    andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsforholdRef)).get()));
        } else {
            throw new IllegalArgumentException("Beregningsgrunnlag må ha minst ein periode");
        }
    }

    public static boolean erKortvarigYrkesaktivitetSomAvsluttesEtterSkjæringstidspunkt(YrkesaktivitetFilterDto filter, BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                       YrkesaktivitetDto yrkesaktivitet) {
        List<AktivitetsAvtaleDto> ansettelsesPerioder = filter.getAnsettelsesPerioder(yrkesaktivitet);
        List<LocalDateSegment<Boolean>> periodeSegmenter = ansettelsesPerioder
            .stream()
            .map(AktivitetsAvtaleDto::getPeriode)
            .map(a -> new LocalDateSegment<>(a.getFomDato(), a.getTomDato(), true))
            .collect(Collectors.toList());

        LocalDateTimeline<Boolean> ansettelsesTidslinje = new LocalDateTimeline<>(periodeSegmenter, håndterOverlapp()).compress();

        return ansettelsesTidslinje.getDatoIntervaller()
            .stream()
            .filter(avtale -> starterFørOgSlutterEtter(beregningsgrunnlag.getSkjæringstidspunkt(), avtale))
            .anyMatch(KortvarigArbeidsforholdTjeneste::isDurationLessThan6Months);
    }

    private static boolean brukerHarStatusSN(BeregningsgrunnlagDto beregningsgrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder()
                .stream()
                .map(BeregningsgrunnlagPeriodeDto::getBeregningsgrunnlagPrStatusOgAndelList)
                .flatMap(Collection::stream)
                .anyMatch(bpsa -> bpsa.getAktivitetStatus().erSelvstendigNæringsdrivende());
    }

    private static Optional<YrkesaktivitetDto> finnKorresponderendeYrkesaktivitet(Collection<YrkesaktivitetDto> kortvarigeArbeidsforhold,
                                                                                  Optional<Arbeidsgiver> arbeidsgiverOpt,
                                                                                  Optional<InternArbeidsforholdRefDto> arbeidsforholdRefOpt) {

        return arbeidsgiverOpt.flatMap(arbeidsgiver -> kortvarigeArbeidsforhold.stream()
                .filter(ya -> ya.gjelderFor(arbeidsgiver, arbeidsforholdRefOpt.orElse(InternArbeidsforholdRefDto.nullRef())))
                .findFirst());
    }

    private static Collection<YrkesaktivitetDto> hentKortvarigeYrkesaktiviteter(BeregningsgrunnlagDto beregningsgrunnlag,
                                                                                InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag) {
        var filter = hentYrkesaktiviteter(inntektArbeidYtelseGrunnlag, beregningsgrunnlag.getSkjæringstidspunkt());
        Collection<YrkesaktivitetDto> yrkesAktiviteterOrdArb = filter.getYrkesaktiviteterForBeregning().stream()
                .filter(ya -> ya.getArbeidType().equals(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)).collect(Collectors.toList());

        return yrkesAktiviteterOrdArb.stream()
                .filter(ya -> erKortvarigYrkesaktivitetSomAvsluttesEtterSkjæringstidspunkt(filter, beregningsgrunnlag, ya))
                .collect(Collectors.toList());
    }

    private static YrkesaktivitetFilterDto hentYrkesaktiviteter(InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                LocalDate skjæringstidspunkt) {
        var aktørArbeid = inntektArbeidYtelseGrunnlag.getAktørArbeidFraRegister();
        var filter = new YrkesaktivitetFilterDto(inntektArbeidYtelseGrunnlag.getArbeidsforholdInformasjon(), aktørArbeid);
        return filter.før(skjæringstidspunkt);
    }

    private static LocalDateSegmentCombinator<Boolean, Boolean, Boolean> håndterOverlapp() {
        return (interlal, segment1, segment2) -> new LocalDateSegment<>(interlal, true);
    }

    private static boolean starterFørOgSlutterEtter(LocalDate skjæringstidspunkt, LocalDateInterval avtale) {
        return avtale.getFomDato().isBefore(skjæringstidspunkt) && !avtale.getTomDato().isBefore(skjæringstidspunkt);
    }

    private static boolean isDurationLessThan6Months(LocalDateInterval aa) {
        Period duration = aa.getFomDato().until(aa.getTomDato().plusDays(1));
        return duration.getYears() < 1 && duration.getMonths() < 6;
    }
}
