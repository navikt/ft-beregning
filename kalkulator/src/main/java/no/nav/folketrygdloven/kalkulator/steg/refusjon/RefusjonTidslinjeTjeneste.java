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

    public static LocalDateTimeline<RefusjonPeriode> lagTidslinje(BeregningsgrunnlagDto beregningsgrunnlag, boolean utbetalt, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return beregningsgrunnlag.getBeregningsgrunnlagPerioder()
                .stream()
                .map(periode -> new LocalDateSegment<>(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom(), lagRefusjonsperiode(periode, utbetalt, ytelsespesifiktGrunnlag)))
                .collect(Collectors.collectingAndThen(Collectors.toList(), s -> new LocalDateTimeline<>(s).compress()));
    }

    private static RefusjonPeriode lagRefusjonsperiode(BeregningsgrunnlagPeriodeDto periode, boolean utbetalt, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        List<RefusjonAndel> andeler = lagAndelsliste(periode.getBeregningsgrunnlagPrStatusOgAndelList(), utbetalt, periode.getPeriode(), ytelsespesifiktGrunnlag);
        return new RefusjonPeriode(periode.getBeregningsgrunnlagPeriodeFom(), periode.getBeregningsgrunnlagPeriodeTom(), andeler);
    }

    private static List<RefusjonAndel> lagAndelsliste(List<BeregningsgrunnlagPrStatusOgAndelDto> bgAndeler, boolean utbetalt, Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return bgAndeler.stream()
                .map(a -> new RefusjonAndel(a.getAktivitetStatus(), a.getArbeidsgiver().orElse(null), a.getArbeidsforholdRef().orElse(null),
                        getBrutto(a, utbetalt, harUtbetalingForAndelIPeriode(a, periode, ytelsespesifiktGrunnlag)),
                        getRefusjonskravPrÅr(a, utbetalt)))
                .collect(Collectors.toList());
    }

    private static boolean harUtbetalingForAndelIPeriode(BeregningsgrunnlagPrStatusOgAndelDto andel, Intervall periode, YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        return UtbetalingsgradTjeneste.finnUtbetalingsgradForAndel(andel, periode, ytelsespesifiktGrunnlag, false).compareTo(Utbetalingsgrad.ZERO) > 0;
    }

    private static Beløp getBrutto(BeregningsgrunnlagPrStatusOgAndelDto a, boolean gjelderOriginaltGrunnlag, boolean skalHaUtbetaling) {
        // Tar hensyn til om det er søkt om ytelse i original behandling. Dersom det ikke er søkt vil dagsats vere 0.
        // Dersom man ikke ser bort fra disse vil nye søknader kunne tolkes som et tilbaketrekk fra bruker.
        // Forklaringen på dette er at brutto > 0 selv om utbetalingsgrad = 0, men refusjonskravPrÅr = 0 når utbetalingsgrad = 0.
        // Ref: https://jira.adeo.no/browse/TSF-2590
        var gjelderOriginaltIkkeUtbetaltAndel = gjelderOriginaltGrunnlag && (a.getDagsats() == null || a.getDagsats() == 0L);
        var gjelderRevurdertIkkeUtbetaltAndel = !gjelderOriginaltGrunnlag && !skalHaUtbetaling;
        if (gjelderOriginaltIkkeUtbetaltAndel || gjelderRevurdertIkkeUtbetaltAndel) {
            return Beløp.ZERO;
        } else {
            return a.getBruttoPrÅr() == null ? Beløp.ZERO : a.getBruttoPrÅr();
        }
    }

    private static Beløp getRefusjonskravPrÅr(BeregningsgrunnlagPrStatusOgAndelDto andel, boolean utbetalt) {
        if (utbetalt) {
            return andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getGjeldendeRefusjonPrÅr).orElse(Beløp.ZERO);
        }
        return andel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforholdDto::getInnvilgetRefusjonskravPrÅr).orElse(Beløp.ZERO);
    }


    public static LocalDateTimeline<RefusjonPeriodeEndring> kombinerTidslinjer(LocalDateTimeline<RefusjonPeriode> originalePerioder, LocalDateTimeline<RefusjonPeriode> revurderingPerioder) {
        return originalePerioder.intersection(revurderingPerioder, (dateInterval, segment1, segment2) ->
        {
            RefusjonPeriode orignalPeriode = segment1.getValue();
            RefusjonPeriode revurderingPeriode = segment2.getValue();
            RefusjonPeriodeEndring refusjonPeriodeEndring = new RefusjonPeriodeEndring(orignalPeriode.getAndeler(), revurderingPeriode.getAndeler());
            return new LocalDateSegment<>(dateInterval, refusjonPeriodeEndring);
        });
    }

}
