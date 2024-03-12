package no.nav.folketrygdloven.kalkulator.ytelse.k9;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse.MapRefusjonPerioderFraVLTilRegelUtbgrad;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public class MapRefusjonPerioderFraVLTilRegelPleiepenger extends MapRefusjonPerioderFraVLTilRegelUtbgrad {


    public MapRefusjonPerioderFraVLTilRegelPleiepenger() {
        super();
    }

    /**
     * Finner gyldige perioder for refusjon basert på perioder med utbetalingsgrad og ansettelse
     *
     * @param startdatoEtterPermisjon   Startdato for permisjonen for ytelse søkt for
     * @param ytelsespesifiktGrunnlag   Ytelsesspesifikt grunnlag
     * @param im                        inntektsmelding for refusjonskrav
     * @param relaterteYrkesaktiviteter Relaterte yrkesaktiviteter
     * @return Gyldige perioder for refusjon
     */
    @Override
    protected List<Intervall> finnGyldigeRefusjonPerioder(LocalDate startdatoEtterPermisjon,
                                                          YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag,
                                                          InntektsmeldingDto im,
                                                          List<AktivitetsAvtaleDto> ansattperioder,
                                                          Set<YrkesaktivitetDto> relaterteYrkesaktiviteter) {


        if (KonfigurasjonVerdi.instance().get("PSB_IKKE_OPPHOER_REFUSJON_VED_ENDT_ARBEIDSFORHOLD", false)) {
            return super.finnGyldigeRefusjonPerioder(startdatoEtterPermisjon, ytelsespesifiktGrunnlag, im, ansattperioder, relaterteYrkesaktiviteter);
        }

        if (im.getRefusjonOpphører() != null && im.getRefusjonOpphører().isBefore(startdatoEtterPermisjon)) {
            // Refusjon opphører før det utledede startpunktet, blir aldri refusjon
            return Collections.emptyList();
        }
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            var utbetalingTidslinje = finnUtbetalingTidslinje((UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag, im, startdatoEtterPermisjon);
            var ansettelseTidslinje = finnAnsettelseTidslinje(ansattperioder);
            return utbetalingTidslinje.intersection(ansettelseTidslinje)
                    .getLocalDateIntervals()
                    .stream()
                    .map(i -> Intervall.fraOgMedTilOgMed(i.getFomDato(), i.getTomDato()))
                    .collect(Collectors.toList());
        }
        throw new IllegalStateException("Forventet utbetalingsgrader men fant ikke UtbetalingsgradGrunnlag.");
    }

    private LocalDateTimeline<Boolean> finnAnsettelseTidslinje(List<AktivitetsAvtaleDto> ansattperioder) {
        var segmenterMedAnsettelse = ansattperioder.stream()
                .map(AktivitetsAvtaleDto::getPeriode)
                .map(p -> new LocalDateTimeline<>(List.of(new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), true))))
                .collect(Collectors.toList());

        var timeline = new LocalDateTimeline<Boolean>(List.of());

        for (LocalDateTimeline<Boolean> localDateSegments : segmenterMedAnsettelse) {
            timeline = timeline.combine(localDateSegments, StandardCombinators::coalesceRightHandSide, LocalDateTimeline.JoinStyle.CROSS_JOIN);
        }

        return timeline.compress();
    }

    private LocalDateTimeline<Boolean> finnUtbetalingTidslinje(UtbetalingsgradGrunnlag ytelsespesifiktGrunnlag, InntektsmeldingDto im, LocalDate startDatoEtterPermisjon) {
        final List<LocalDateTimeline<Boolean>> segmenterMedUtbetaling = UtbetalingsgradTjeneste.finnPerioderForArbeid(ytelsespesifiktGrunnlag, im.getArbeidsgiver(), im.getArbeidsforholdRef(), true)
                .stream()
                .flatMap(u -> u.getPeriodeMedUtbetalingsgrad().stream())
                .filter(p -> p.getUtbetalingsgrad() != null && p.getUtbetalingsgrad().compareTo(Utbetalingsgrad.ZERO) > 0
                        || harAktivitetsgradMedTilkommetInntektToggle(p))
                .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                .map(p -> new LocalDateTimeline<>(List.of(new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), true))))
                .collect(Collectors.toList());

        var timeline = new LocalDateTimeline<Boolean>(List.of());

        for (LocalDateTimeline<Boolean> localDateSegments : segmenterMedUtbetaling) {
            timeline = timeline.combine(localDateSegments, StandardCombinators::coalesceRightHandSide, LocalDateTimeline.JoinStyle.CROSS_JOIN);
        }

        var tidslinjeEtterPermisjon = new LocalDateTimeline<>(List.of(new LocalDateSegment<>(startDatoEtterPermisjon, TIDENES_ENDE, Boolean.TRUE)));
        timeline = timeline.intersection(tidslinjeEtterPermisjon);

        return timeline.compress();
    }

    private static boolean harAktivitetsgradMedTilkommetInntektToggle(PeriodeMedUtbetalingsgradDto p) {
        return p.getAktivitetsgrad().map(ag -> ag.compareTo(Aktivitetsgrad.HUNDRE) < 0).orElse(false)
                && KonfigurasjonVerdi.instance().get("GRADERING_MOT_INNTEKT", false);
    }


}
