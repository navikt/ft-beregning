package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnFørsteDagEtterPermisjon;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon.MapRefusjonPerioderFraVLTilRegel;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonFilter;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public class MapRefusjonPerioderFraVLTilRegelUtbgrad
        extends MapRefusjonPerioderFraVLTilRegel {

    public MapRefusjonPerioderFraVLTilRegelUtbgrad() {
        super();
    }

    @Override
    protected Optional<LocalDate> utledStartdatoEtterPermisjon(LocalDate skjæringstidspunktBeregning,
                                                               InntektsmeldingDto inntektsmelding,
                                                               Set<YrkesaktivitetDto> yrkesaktiviteterForIM,
                                                               PermisjonFilter permisjonFilter,
                                                               YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        var førsteSøktePermisjonsdag = finnFørsteSøktePermisjonsdag(
                yrkesaktiviteterForIM, permisjonFilter,
                skjæringstidspunktBeregning,
                (UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag);
        return førsteSøktePermisjonsdag.map(dato -> skjæringstidspunktBeregning.isAfter(dato) ? skjæringstidspunktBeregning : dato);
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
        if (im.getRefusjonOpphører() != null && im.getRefusjonOpphører().isBefore(startdatoEtterPermisjon)) {
            // Refusjon opphører før det utledede startpunktet, blir aldri refusjon
            return Collections.emptyList();
        }
        if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
            var utbetalingTidslinje = finnUtbetalingTidslinje((UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag, im);
            return utbetalingTidslinje
                    .getLocalDateIntervals()
                    .stream()
                    .map(i -> Intervall.fraOgMedTilOgMed(i.getFomDato(), i.getTomDato()))
                    .collect(Collectors.toList());
        }
        throw new IllegalStateException("Forventet utbetalingsgrader men fant ikke UtbetalingsgradGrunnlag.");
    }

    private LocalDateTimeline<Boolean> finnUtbetalingTidslinje(UtbetalingsgradGrunnlag ytelsespesifiktGrunnlag, InntektsmeldingDto im) {
        final List<LocalDateTimeline<Boolean>> segmenterMedUtbetaling = UtbetalingsgradTjeneste.finnPerioderForArbeid(ytelsespesifiktGrunnlag, im.getArbeidsgiver(), im.getArbeidsforholdRef(), false)
                .stream()
                .flatMap(u -> u.getPeriodeMedUtbetalingsgrad().stream())
                .filter(p -> p.getUtbetalingsgrad() != null && p.getUtbetalingsgrad().compareTo(Utbetalingsgrad.ZERO) > 0
                        || harAktivitetsgradMedTilkommetInntektToggle(p))
                .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                .map(p -> new LocalDateTimeline<>(List.of(new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), true))))
                .toList();

        var timeline = new LocalDateTimeline<Boolean>(List.of());

        for (LocalDateTimeline<Boolean> localDateSegments : segmenterMedUtbetaling) {
            timeline = timeline.combine(localDateSegments, StandardCombinators::coalesceRightHandSide, LocalDateTimeline.JoinStyle.CROSS_JOIN);
        }

        return timeline.compress();
    }

    private static boolean harAktivitetsgradMedTilkommetInntektToggle(PeriodeMedUtbetalingsgradDto p) {
        return p.getAktivitetsgrad().map(ag -> ag.compareTo(Aktivitetsgrad.HUNDRE) < 0).orElse(false)
                && KonfigurasjonVerdi.instance().get("GRADERING_MOT_INNTEKT", false);
    }


    private Optional<LocalDate> finnFørsteSøktePermisjonsdag(Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                             PermisjonFilter permisjonFilter,
                                                             LocalDate skjæringstidspunkt,
                                                             UtbetalingsgradGrunnlag ytelsespesifiktGrunnlag) {
        var alleAnsattperioderForInntektsmeldingEtterStartAvBeregning = finnAnsattperioderForYrkesaktiviteter(yrkesaktiviteter, skjæringstidspunkt);
        var ansettelsesPeriode = FinnAnsettelsesPeriode.getMinMaksPeriode(alleAnsattperioderForInntektsmeldingEtterStartAvBeregning, skjæringstidspunkt);
        var førstedagEtterPermisjonOpt = FinnFørsteDagEtterPermisjon.finn(yrkesaktiviteter, ansettelsesPeriode, skjæringstidspunkt, permisjonFilter);
        if (førstedagEtterPermisjonOpt.isEmpty()) {
            return Optional.empty();
        }
        var utbetalingsgrader = yrkesaktiviteter.stream().map(ya -> ytelsespesifiktGrunnlag.finnUtbetalingsgraderForArbeid(ya.getArbeidsgiver(), ya.getArbeidsforholdRef()))
                .flatMap(Collection::stream)
                .toList();
        Optional<LocalDate> førsteDatoMedUtbetalingOpt = utbetalingsgrader.stream()
                .filter(periodeMedUtbetalingsgradDto -> periodeMedUtbetalingsgradDto.getUtbetalingsgrad().compareTo(Utbetalingsgrad.ZERO) != 0)
                .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                .map(Intervall::getFomDato)
                .min(Comparator.naturalOrder());

        if (førsteDatoMedUtbetalingOpt.isEmpty()) {
            return Optional.empty();
        }

        LocalDate førsteDagEtterPermisjon = førstedagEtterPermisjonOpt.get();
        LocalDate førsteDatoMedUtbetaling = førsteDatoMedUtbetalingOpt.get();
        return førsteDagEtterPermisjon.isAfter(førsteDatoMedUtbetaling) ? førstedagEtterPermisjonOpt : førsteDatoMedUtbetalingOpt;
    }

}
