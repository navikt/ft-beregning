package no.nav.folketrygdloven.besteberegning;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.RelatertYtelseType;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;
import no.nav.folketrygdloven.besteberegning.modell.input.YtelseAktivitetType;
import no.nav.folketrygdloven.besteberegning.modell.input.Ytelsegrunnlag;
import no.nav.folketrygdloven.besteberegning.modell.input.YtelsegrunnlagPeriode;
import no.nav.folketrygdloven.besteberegning.modell.output.AktivitetNøkkel;
import no.nav.folketrygdloven.besteberegning.modell.output.Inntekt;

/**
 * Tjeneste som skal fastsette hvot stor andel hver aktivitet bidrar til for en ytelse.
 * For eksempel hvis ytelsen er foreldrepenger, og perioden er 01.01.2020 - 01.02.2020, må vi undersøke foreldrepengene
 * i denne perioden og finne ut hvordan den fordeler seg over ulike andeler
 */
public class FastsettYtelseFordeling {

    private FastsettYtelseFordeling() {
        // Skjuler default konstruktør
    }

    public static List<Inntekt> fordelYtelse(List<Ytelsegrunnlag> alleYtelsegrunnlag, Periodeinntekt periodeinntekt) {
        var ytelseperioder = søkEtterYtelseMedOverlappIPeriode(alleYtelsegrunnlag, periodeinntekt.getYtelse(), periodeinntekt.getPeriode());
        if (!ytelseperioder.isEmpty()) {
            return fordelInntekt(ytelseperioder, periodeinntekt);
        }
        // Av og til utbetales en ytelse i måneden etter vedtaket er gjeldende, f.eks ved forsinket saksbehandling.
        // Sjekker derfor måneden før også om vi ikke finner noe ytelse i måneden inntekten gjelder for
        var utvidetPeriode = Periode.of(periodeinntekt.getFom().minusMonths(1), periodeinntekt.getTom());
        var ytelseperioderUtvidet = søkEtterYtelseMedOverlappIPeriode(alleYtelsegrunnlag, periodeinntekt.getYtelse(), utvidetPeriode);
        if (!ytelseperioderUtvidet.isEmpty()) {
            return fordelInntekt(ytelseperioderUtvidet, periodeinntekt, utvidetPeriode);
        }

        // Finner ingen overlappende ytelsesperiode, ignorerer inntekten
        return Collections.emptyList();
    }

    private static List<Inntekt> fordelInntekt(List<YtelsegrunnlagPeriode> ytelseperioder, Periodeinntekt periodeinntekt) {
        return fordelInntekt(ytelseperioder, periodeinntekt, periodeinntekt.getPeriode());
    }

    private static List<Inntekt> fordelInntekt(List<YtelsegrunnlagPeriode> ytelseperioder,
                                               Periodeinntekt periodeinntekt,
                                               Periode periodeInntektErTjentI) {
        if (RelatertYtelseType.SYKEPENGER.equals(periodeinntekt.getYtelse())) {
            return utledFordelingSykepenger(ytelseperioder, periodeinntekt, periodeInntektErTjentI);
        } else if (RelatertYtelseType.FORELDREPENGER.equals(periodeinntekt.getYtelse()) || RelatertYtelseType.SVANGERSKAPSPENGER.equals(
            periodeinntekt.getYtelse())) {
            return utledFordelingK14Ytelser(ytelseperioder, periodeinntekt, periodeInntektErTjentI);
        }
        throw new IllegalStateException(
            "Fått inn ytelse på inntekt vi ikke kan fordele," + " ytelsetype var " + periodeinntekt.getYtelse() + " for periode "
                + periodeinntekt.getPeriode());
    }

    private static List<Inntekt> utledFordelingK14Ytelser(List<YtelsegrunnlagPeriode> ytelseperioder,
                                                          Periodeinntekt periodeinntekt,
                                                          Periode periodeInntektErTjentI) {
        validerK14Ytelser(ytelseperioder);
        var aktivitetInntektMap = new EnumMap<YtelseAktivitetType, BigDecimal>(YtelseAktivitetType.class);
        ytelseperioder.forEach(ytelseperiode -> {
            var overlapp = finnOverlappMellomPerioder(ytelseperiode.getPeriode(), periodeInntektErTjentI);
            overlapp.ifPresent(op -> ytelseperiode.getAndeler().forEach(ytelseAndel -> {
                var virkedagerIPeriode = Virkedager.beregnAntallVirkedager(op);
                var totalUtbetalingForAndelIPeriode = ytelseAndel.getDagsats().multiply(BigDecimal.valueOf(virkedagerIPeriode));
                var aggregertInntekt = aktivitetInntektMap.getOrDefault(ytelseAndel.getAktivitet(), BigDecimal.ZERO);
                aktivitetInntektMap.put(ytelseAndel.getAktivitet(), aggregertInntekt.add(totalUtbetalingForAndelIPeriode));
            }));
        });

        var sumYtelseIOverlappendePeriode = aktivitetInntektMap.values().stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

        List<Inntekt> inntekter = new ArrayList<>();
        aktivitetInntektMap.forEach((key, value) -> {
            var andel = sumYtelseIOverlappendePeriode.compareTo(BigDecimal.ZERO) > 0 ? value.divide(sumYtelseIOverlappendePeriode, 10,
                RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
            var andelsBeløpAvYtelse = periodeinntekt.getInntekt().multiply(andel);
            var aktivitetNøkkel = lagK14Aktivitet(periodeinntekt.getYtelse(), key);
            inntekter.add(new Inntekt(aktivitetNøkkel, andelsBeløpAvYtelse));
        });
        verifiser(inntekter, periodeinntekt.getInntekt());
        return inntekter;
    }

    private static AktivitetNøkkel lagK14Aktivitet(RelatertYtelseType ytelse, YtelseAktivitetType key) {
        if (ytelse.equals(RelatertYtelseType.FORELDREPENGER)) {
            return AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.FORELDREPENGER_MOTTAKER, key);
        } else {
            return AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.SVANGERSKAPSPENGER_MOTTAKER, key);
        }
    }

    private static List<YtelsegrunnlagPeriode> søkEtterYtelseMedOverlappIPeriode(List<Ytelsegrunnlag> alleYtelsegrunnlag,
                                                                                 RelatertYtelseType ytelseType,
                                                                                 Periode periodeViSkalOverlappeMed) {
        return alleYtelsegrunnlag.stream()
            .filter(yt -> yt.getYtelse().equals(ytelseType))
            .map(Ytelsegrunnlag::getPerioder)
            .flatMap(Collection::stream)
            .filter(ytelsePeriode -> overlapperInntektsperiode(periodeViSkalOverlappeMed, ytelsePeriode))
            .toList();
    }

    private static List<Inntekt> utledFordelingSykepenger(List<YtelsegrunnlagPeriode> perioderSomOverlapperInntektsperiode,
                                                          Periodeinntekt periodeinntekt,
                                                          Periode periodeInntektErTjentI) {
        validerSykepengeytele(perioderSomOverlapperInntektsperiode);

        var sykepengeperioderPrAktivitetMap = new EnumMap<YtelseAktivitetType, List<Periode>>(YtelseAktivitetType.class);
        var totaltOverlappendeDager = BigDecimal.valueOf(finnTotalVarighetOverlapp(perioderSomOverlapperInntektsperiode, periodeInntektErTjentI));

        for (var ytelseperiode : perioderSomOverlapperInntektsperiode) {
            var overlapp = finnOverlappMellomPerioder(ytelseperiode.getPeriode(), periodeInntektErTjentI);
            overlapp.ifPresent(overlappendePeriode -> {
                var andelPåYtelse = ytelseperiode.getAndeler().get(0);
                var sykepengeperiodePåAkvititet = sykepengeperioderPrAktivitetMap.getOrDefault(andelPåYtelse.getAktivitet(), new ArrayList<>());
                sykepengeperiodePåAkvititet.add(overlappendePeriode);
                sykepengeperioderPrAktivitetMap.put(andelPåYtelse.getAktivitet(), sykepengeperiodePåAkvititet);
            });
        }

        // For hver nøkkel må vi finne ut hvor stor andel av total overlappende periode de bidrar med
        List<Inntekt> inntekter = new ArrayList<>();
        sykepengeperioderPrAktivitetMap.forEach((key, value) -> {
            var varighetForAktivitet = BigDecimal.valueOf(value.stream().map(Periode::getVarighetDager).reduce(Long::sum).orElse(0L));
            var andel = varighetForAktivitet.divide(totaltOverlappendeDager, 10, RoundingMode.HALF_EVEN);
            var andelsBeløpAvYtelse = periodeinntekt.getInntekt().multiply(andel);
            var aktivitetNøkkel = AktivitetNøkkel.forYtelseFraSammenligningsfilter(Aktivitet.SYKEPENGER_MOTTAKER, key);
            inntekter.add(new Inntekt(aktivitetNøkkel, andelsBeløpAvYtelse));
        });
        verifiser(inntekter, periodeinntekt.getInntekt());
        return inntekter;
    }

    private static void verifiser(List<Inntekt> fordelteInntekter, BigDecimal inntektÅFordele) {
        var fordeltInntekt = fordelteInntekter.stream().map(Inntekt::getInntektPrMåned).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        var erDiff = inntektÅFordele.compareTo(fordeltInntekt) != 0;
        if (erDiff) {
            throw new IllegalStateException(
                "Fordelt inntekt og fastsatt inntekt er ikke lik." + " Fordelt inntekt er " + fordeltInntekt.toPlainString()
                    + " mens inntekt å fordele er " + inntektÅFordele.toPlainString());
        }
    }

    private static void validerSykepengeytele(List<YtelsegrunnlagPeriode> sykepengeperioder) {
        var finnesPeriodeMedUgyldigAndelsstørrelse = sykepengeperioder.stream().anyMatch(p -> p.getAndeler().size() != 1);
        if (finnesPeriodeMedUgyldigAndelsstørrelse) {
            throw new IllegalStateException(
                "Finnes ikke akkurat 1 sykepengeandel for minst en periode. Sykepengeperiodene som undersøkes er " + sykepengeperioder);
        }
    }

    private static void validerK14Ytelser(List<YtelsegrunnlagPeriode> ytelseperioder) {
        var finnesUgyldigPeriode = ytelseperioder.stream()
            .anyMatch(yp -> yp.getAndeler().stream().anyMatch(a -> a.getAktivitet() == null || a.getDagsats() == null));
        if (finnesUgyldigPeriode) {
            throw new IllegalStateException("Finnes ytelseperiode uten dagsats eller aktivitet. Ytelseperioder: " + ytelseperioder);
        }
    }

    private static long finnTotalVarighetOverlapp(List<YtelsegrunnlagPeriode> perioderSomOverlapperInntektsperiode, Periode inntektsperiode) {
        return perioderSomOverlapperInntektsperiode.stream()
            .map(periode -> finnOverlappMellomPerioder(periode.getPeriode(), inntektsperiode).map(Periode::getVarighetDager).orElse(0L))
            .reduce(Long::sum)
            .orElse(0L);
    }

    private static boolean overlapperInntektsperiode(Periode inntektsperiode, YtelsegrunnlagPeriode ytelsePeriode) {
        return inntektsperiode.overlapper(ytelsePeriode.getPeriode());
    }

    private static Optional<Periode> finnOverlappMellomPerioder(Periode periode1, Periode periode2) {
        if (!periode1.overlapper(periode2)) {
            return Optional.empty();
        }
        var fom = periode1.getFom().isBefore(periode2.getFom()) ? periode2.getFom() : periode1.getFom();
        var tom = periode1.getTom().isBefore(periode2.getTom()) ? periode1.getTom() : periode2.getTom();
        return Optional.of(Periode.of(fom, tom));
    }

}
