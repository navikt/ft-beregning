package no.nav.folketrygdloven.kalkulator.guitjenester.fakta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;

class InntektForAndelTjeneste {

    private InntektForAndelTjeneste() {
        // Hide public constructor
    }

    static Optional<Beløp> finnSnittinntektForArbeidstakerIBeregningsperioden(InntektFilterDto filter, no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel) {
        LocalDate tilDato = andel.getBeregningsperiodeTom();
        long beregningsperiodeLengdeIMnd = finnHeleMåneder(andel.getBeregningsperiode());
        if (beregningsperiodeLengdeIMnd == 0) {
            return Optional.empty();
        }
        var totalBeløp = finnTotalbeløpIBeregningsperioden(filter, andel, tilDato, beregningsperiodeLengdeIMnd);
        return Optional.of(totalBeløp).map(tb -> tb.divider(BigDecimal.valueOf(beregningsperiodeLengdeIMnd), 10, RoundingMode.HALF_EVEN));
    }

    private static int finnHeleMåneder(Intervall periode) {
        int antallMåneder = 0;
        LocalDate date = periode.getFomDato().minusDays(1).with(TemporalAdjusters.lastDayOfMonth());
        while (date.isBefore(periode.getTomDato())) {
            antallMåneder++;
            date = date.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        }
        return antallMåneder;
    }


    private static Beløp finnTotalbeløpIBeregningsperioden(InntektFilterDto filter, no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel, LocalDate tilDato,
                                                                Long beregningsperiodeLengdeIMnd) {
        if (filter.isEmpty()) {
            return Beløp.ZERO;
        }
        var inntekter = finnInntekterForAndel(andel, filter);

        AtomicReference<Beløp> totalBeløp = new AtomicReference<>(Beløp.ZERO);
        inntekter.forFilter((inntekt, inntektsposter) -> totalBeløp
                .set(totalBeløp.get().adder(summerInntekterIBeregningsperioden(tilDato, inntektsposter, beregningsperiodeLengdeIMnd))));

        return totalBeløp.get();
    }

    static Optional<Beløp> finnSnittinntektPrÅrForArbeidstakerIBeregningsperioden(InntektFilterDto filter, no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel) {
        if (filter.isEmpty()) {
            return Optional.of(Beløp.ZERO);
        }
        LocalDate fraDato = andel.getBeregningsperiodeFom();
        LocalDate tilDato = andel.getBeregningsperiodeTom();
        long beregningsperiodeLengdeIMnd = ChronoUnit.MONTHS.between(fraDato, tilDato.plusDays(1));
        if (beregningsperiodeLengdeIMnd == 0) {
            return Optional.empty();
        }
        var totalBeløp = finnTotalbeløpIBeregningsperioden(filter, andel, tilDato, beregningsperiodeLengdeIMnd);
        var faktor = KonfigTjeneste.getMånederIÅr().divide(BigDecimal.valueOf(beregningsperiodeLengdeIMnd), 10, RoundingMode.HALF_EVEN);
        return Optional.of(totalBeløp.multipliser(faktor));
    }

    private static InntektFilterDto finnInntekterForAndel(no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto andel, InntektFilterDto filter) {
        Optional<Arbeidsgiver> arbeidsgiver = andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getArbeidsgiver);
        if (arbeidsgiver.isEmpty()) {
            return InntektFilterDto.EMPTY;
        }
        return filter.filterBeregningsgrunnlag()
                .filter(arbeidsgiver.get());
    }

    private static Beløp summerInntekterIBeregningsperioden(LocalDate tilDato, Collection<InntektspostDto> inntektsposter, Long beregningsperiodeLengdeIMnd) {
        Beløp totalBeløp = Beløp.ZERO;
        for (int måned = 0; måned < beregningsperiodeLengdeIMnd; måned++) {
            LocalDate dato = tilDato.minusMonths(måned);
            var beløp = finnMånedsinntekt(inntektsposter, dato);
            totalBeløp = totalBeløp.adder(beløp);
        }
        return totalBeløp;
    }

    private static Beløp finnMånedsinntekt(Collection<InntektspostDto> inntektsposter, LocalDate dato) {
        return inntektsposter.stream()
                .filter(inntektspost -> inntektspost.getPeriode().inkluderer(dato))
                .findFirst().map(InntektspostDto::getBeløp).orElse(Beløp.ZERO);
    }

    static Optional<Beløp> finnSnittAvFrilansinntektIBeregningsperioden(InntektArbeidYtelseGrunnlagDto grunnlag,
                                                                             no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto frilansAndel, LocalDate skjæringstidspunkt) {
        var filter = new InntektFilterDto(grunnlag.getAktørInntektFraRegister()).før(skjæringstidspunkt);
        if (!filter.isEmpty()) {
            LocalDate fraDato = frilansAndel.getBeregningsperiodeFom();
            LocalDate tilDato = frilansAndel.getBeregningsperiodeTom();
            long beregningsperiodeLengdeIMnd = ChronoUnit.MONTHS.between(fraDato, tilDato.plusDays(1));
            List<YrkesaktivitetDto> yrkesaktiviteter = finnYrkesaktiviteter(grunnlag, skjæringstidspunkt);
            boolean erFrilanser = yrkesaktiviteter.stream().anyMatch(ya -> ArbeidType.FRILANSER.equals(ya.getArbeidType()));

            var frilansInntekter = filter.filterBeregningsgrunnlag().filter(inntekt -> {
                var arbeidTyper = getArbeidTyper(yrkesaktiviteter, inntekt.getArbeidsgiver());
                return erFrilansInntekt(arbeidTyper, erFrilanser);
            });

            if (frilansInntekter.isEmpty()) {
                return Optional.empty();
            }
            AtomicReference<Beløp> totalBeløp = new AtomicReference<>(Beløp.ZERO);
            frilansInntekter.forFilter((inntekt, inntektsposter) -> totalBeløp
                    .set(totalBeløp.get().adder(summerInntekterIBeregningsperioden(tilDato, inntektsposter, beregningsperiodeLengdeIMnd))));
            return Optional.of(totalBeløp.get().divider(BigDecimal.valueOf(beregningsperiodeLengdeIMnd), 10, RoundingMode.HALF_EVEN));

        }
        return Optional.empty();
    }

    private static List<YrkesaktivitetDto> finnYrkesaktiviteter(InntektArbeidYtelseGrunnlagDto inntektArbeidYtelseGrunnlag,
                                                                LocalDate skjæringstidspunkt) {
        List<YrkesaktivitetDto> yrkesaktiviteter = new ArrayList<>();

        var aktørArbeid = inntektArbeidYtelseGrunnlag.getAktørArbeidFraRegister();

        var filterRegister = new YrkesaktivitetFilterDto(inntektArbeidYtelseGrunnlag.getArbeidsforholdInformasjon(), aktørArbeid).før(skjæringstidspunkt);
        yrkesaktiviteter.addAll(filterRegister.getYrkesaktiviteterForBeregning());
        yrkesaktiviteter.addAll(filterRegister.getFrilansOppdrag());

        var bekreftetAnnenOpptjening = inntektArbeidYtelseGrunnlag.getBekreftetAnnenOpptjening();
        var filterSaksbehandlet = new YrkesaktivitetFilterDto(inntektArbeidYtelseGrunnlag.getArbeidsforholdInformasjon(), bekreftetAnnenOpptjening);
        yrkesaktiviteter.addAll(filterSaksbehandlet.getYrkesaktiviteterForBeregning());

        return yrkesaktiviteter;
    }

    private static Collection<ArbeidType> getArbeidTyper(Collection<YrkesaktivitetDto> yrkesaktiviteter, Arbeidsgiver arbeidsgiver) {
        return yrkesaktiviteter
                .stream()
                .filter(it -> it.getArbeidsgiver() != null)
                .filter(it -> it.getArbeidsgiver().getIdentifikator().equals(arbeidsgiver.getIdentifikator()))
                .map(YrkesaktivitetDto::getArbeidType)
                .distinct()
                .collect(Collectors.toList());
    }

    private static boolean erFrilansInntekt(Collection<ArbeidType> arbeidTyper, boolean erFrilanser) {
        return (arbeidTyper.isEmpty() && erFrilanser) || arbeidTyper.contains(ArbeidType.FRILANSER_OPPDRAGSTAKER);
    }

}
