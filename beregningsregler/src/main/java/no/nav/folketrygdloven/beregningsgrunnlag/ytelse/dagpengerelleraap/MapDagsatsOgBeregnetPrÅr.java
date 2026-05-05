package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.dagpengerelleraap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;

public class MapDagsatsOgBeregnetPrÅr {

    public static MapDagsatsOgBeregnetPrÅr.DagsatsOgBeregnetPrÅr regnUtSnittInntektForDPellerAAP(Inntektsgrunnlag inntektsgrunnlag, AktivitetStatus aktivitetStatus, LocalDate stp, boolean medUtbetalingsfaktor) {
        if (aktivitetStatus.erDPFraYtelse()) {
            var dagpengerFraYtelseVedtak = getDagpengerFraYtelseVedtak(inntektsgrunnlag);
            if (dagpengerFraYtelseVedtak.isPresent()) {
                var dagsats = dagpengerFraYtelseVedtak.get().getInntekt();
                var utbetalingsfaktor = medUtbetalingsfaktor ? dagpengerFraYtelseVedtak.get().getUtbetalingsfaktor().orElseThrow() : BigDecimal.ONE;
                var beregnetPrÅr = dagsats.multiply(Inntektskilde.YTELSE_VEDTAK.getInntektPeriodeType().getAntallPrÅr()).multiply(utbetalingsfaktor);
                return new MapDagsatsOgBeregnetPrÅr.DagsatsOgBeregnetPrÅr(dagsats, beregnetPrÅr);
            }
        }
        var relevanteInntekter = getInntekterForEnkeltdagerFørStpForDPellerAAP(inntektsgrunnlag, stp);
        var beregningsperiodeForYtelse = getBeregningsperiodeForYtelse(relevanteInntekter, stp);
        var antallVirkedager = Virkedager.beregnAntallVirkedager(beregningsperiodeForYtelse);
        var inntekterIBeregningsperiode = relevanteInntekter.stream()
            .filter(pi -> pi.getPeriode().overlapper(beregningsperiodeForYtelse))
            .filter(pi -> Virkedager.beregnAntallVirkedager(pi.getFom(), pi.getTom()) == 1)
            .toList();

        var dagsats = BigDecimal.valueOf(inntektsgrunnlag.getDagsatsYtelseDpAapVedSkjæringstidspunkt());
        var beregnetPrÅr = getBeregnetPrÅr(inntekterIBeregningsperiode, antallVirkedager, dagsats, medUtbetalingsfaktor);
        return new MapDagsatsOgBeregnetPrÅr.DagsatsOgBeregnetPrÅr(dagsats, beregnetPrÅr);
    }

    private static Optional<Periodeinntekt> getDagpengerFraYtelseVedtak(Inntektsgrunnlag inntektsgrunnlag) {
        return inntektsgrunnlag.getSistePeriodeinntekterMedType(Inntektskilde.YTELSE_VEDTAK).stream()
            .filter(i -> i.getInntektskategori().equals(Inntektskategori.DAGPENGER))
            .findFirst();
    }

    private static List<Periodeinntekt> getInntekterForEnkeltdagerFørStpForDPellerAAP(Inntektsgrunnlag inntektsgrunnlag, LocalDate stp) {
        var perioder = inntektsgrunnlag.getPeriodeinntekter().stream()
            .filter(pi -> pi.getInntektPeriodeType().equals(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP.getInntektPeriodeType()))
            .filter(pi -> !pi.getPeriode().getTom().isAfter(stp))
            .toList();
        if (perioder.stream().anyMatch(pi -> !pi.getFom().equals(pi.getTom()))) {
            throw new IllegalStateException("Forventet kun endagersperioder for DP/AAP-beregning, men fant perioder der fom != tom");
        }
        return perioder;
    }

    private static Periode getBeregningsperiodeForYtelse(List<Periodeinntekt> relevanteInntekter, LocalDate stp) {
        var fikspunkt = stp.minusWeeks(1).with(DayOfWeek.SUNDAY);
        var sisteDagMedYtelseUtbetaling = relevanteInntekter.stream()
            .filter(pi -> !pi.getPeriode().getTom().isAfter(fikspunkt))
            .max(Comparator.comparing(pi -> pi.getPeriode().getFom()))
            .or(() -> relevanteInntekter.stream().max(Comparator.comparing(pi -> pi.getPeriode().getFom())))
            .orElseThrow()
            .getFom();
        return Periode.of(sisteDagMedYtelseUtbetaling.minusDays(13), sisteDagMedYtelseUtbetaling);
    }

    private static BigDecimal getBeregnetPrÅr(List<Periodeinntekt> inntekterIBeregningsperiode, int antallVirkedager, BigDecimal dagsats, boolean medUtbetalingsfaktor) {
        var snittDagsats = medUtbetalingsfaktor ? getSnittDagsatsMedUtbetalingsfaktor(inntekterIBeregningsperiode, dagsats, antallVirkedager) : dagsats;
        return snittDagsats.multiply(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP.getInntektPeriodeType().getAntallPrÅr()).setScale(0, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal getSnittDagsatsMedUtbetalingsfaktor(List<Periodeinntekt> inntekterIBeregningsperiode, BigDecimal dagsats, int antallVirkedager) {
        return inntekterIBeregningsperiode.stream()
            .map(pi -> dagsats.multiply(pi.getUtbetalingsfaktor().orElseThrow()))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO)
            .divide(BigDecimal.valueOf(antallVirkedager), 10, RoundingMode.HALF_EVEN);
    }

    public record DagsatsOgBeregnetPrÅr(BigDecimal dagsats, BigDecimal beregnetPrÅr) {
    }
}
