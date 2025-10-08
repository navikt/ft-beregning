package no.nav.folketrygdloven.kalkulator.steg.refusjon;


import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonAndel;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonPeriode;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.modell.RefusjonPeriodeEndring;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class RefusjonTidslinjeTjeneste {

    private RefusjonTidslinjeTjeneste() {
        // Skjuler default
    }

    public static LocalDateTimeline<RefusjonPeriode> lagTidslinje(BeregningsgrunnlagDto beregningsgrunnlag, boolean gjelderForrigeGrunnlag, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder()
                .stream()
                .map(periode -> new LocalDateSegment<>(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom(), lagRefusjonsperiode(periode, gjelderForrigeGrunnlag, ytelsespesifiktGrunnlag)))
                .collect(Collectors.collectingAndThen(Collectors.toList(), s -> new LocalDateTimeline<>(s).compress()));
    }

    private static RefusjonPeriode lagRefusjonsperiode(BeregningsgrunnlagPeriodeDto periode, boolean gjelderForrigeGrunnlag, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var andeler = lagAndelsliste(periode.getBeregningsgrunnlagPrStatusOgAndelList(), gjelderForrigeGrunnlag, periode.getPeriode(), ytelsespesifiktGrunnlag);
        return new RefusjonPeriode(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom(), andeler);
    }

    private static List<RefusjonAndel> lagAndelsliste(List<BeregningsgrunnlagPrStatusOgAndelDto> bgAndeler, boolean gjelderForrigeGrunnlag, Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return bgAndeler.stream()
                .map(andel -> new RefusjonAndel(andel.getAktivitetStatus(), andel.getArbeidsgiver().orElse(null), andel.getArbeidsforholdRef().orElse(null),
                        getBrutto(andel, gjelderForrigeGrunnlag, harUtbetalingForAndelIPeriode(andel, periode, ytelsespesifiktGrunnlag)),
                        getRefusjonskravPrÅr(andel, gjelderForrigeGrunnlag)))
            .toList();
    }

    private static boolean harUtbetalingForAndelIPeriode(BeregningsgrunnlagPrStatusOgAndelDto andel, Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return UtbetalingsgradTjeneste.finnUtbetalingsgradForAndel(andel, periode, ytelsespesifiktGrunnlag, false).compareTo(Utbetalingsgrad.ZERO) > 0;
    }

    private static Beløp getBrutto(BeregningsgrunnlagPrStatusOgAndelDto a, boolean gjelderForrigeGrunnlag, boolean skalHaUtbetaling) {
        // Tar hensyn til om det er søkt om ytelse i original behandling. Dersom det ikke er søkt vil dagsats vere 0.
        // Dersom man ikke ser bort fra disse vil nye søknader kunne tolkes som et tilbaketrekk fra bruker.
        // Forklaringen på dette er at brutto > 0 selv om utbetalingsgrad = 0, men refusjonskravPrÅr = 0 når utbetalingsgrad = 0.
        // Ref: https://jira.adeo.no/browse/TSF-2590
        var gjelderForrigeGrunnlagIkkeUtbetaltAndel = gjelderForrigeGrunnlag && (a.getDagsats() == null || a.getDagsats() == 0L);
        var gjelderRevurdertGrunnlagIkkeUtbetaltAndel = !gjelderForrigeGrunnlag && !skalHaUtbetaling;
        if (gjelderForrigeGrunnlagIkkeUtbetaltAndel || gjelderRevurdertGrunnlagIkkeUtbetaltAndel) {
            return Beløp.ZERO;
        } else {
            return a.getBruttoPrÅr() == null ? Beløp.ZERO : a.getBruttoPrÅr();
        }
    }

    private static Beløp getRefusjonskravPrÅr(BeregningsgrunnlagPrStatusOgAndelDto andel, boolean gjelderForrigeGrunnlag) {
        if (gjelderForrigeGrunnlag) {
            return andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(Beløp.ZERO);
        }
        return andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getInnvilgetRefusjonskravPrÅr).orElse(Beløp.ZERO);
    }


    public static LocalDateTimeline<RefusjonPeriodeEndring> kombinerTidslinjer(LocalDateTimeline<RefusjonPeriode> forrigeUtbetaltTidslinje, LocalDateTimeline<RefusjonPeriode> utbetaltTidslinje) {
        return forrigeUtbetaltTidslinje.intersection(utbetaltTidslinje, (dateInterval, segment1, segment2) ->
        {
            var forrigePeriode = segment1.getValue();
            var periode = segment2.getValue();
            var refusjonPeriodeEndring = new RefusjonPeriodeEndring(forrigePeriode.getAndeler(), periode.getAndeler());
            return new LocalDateSegment<>(dateInterval, refusjonPeriodeEndring);
        });
    }

}
