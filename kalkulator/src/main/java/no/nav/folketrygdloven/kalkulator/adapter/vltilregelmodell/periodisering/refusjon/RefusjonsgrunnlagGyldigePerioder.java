package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnFørsteDagEtterPermisjon;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnStartdatoPermisjon;
import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonFilter;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Utbetalingsgrad;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

final class RefusjonsgrunnlagGyldigePerioder {

    private RefusjonsgrunnlagGyldigePerioder() {
    }

    static List<AktivitetsAvtaleDto> finnAnsattperioder(Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                      LocalDate skjæringstidspunkt) {
        return yrkesaktiviteter.stream()
            .flatMap(ya -> ya.getAlleAnsettelsesperioder().stream())
            .filter(a -> !a.getPeriode().getTomDato().isBefore(BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunkt)))
            .toList();
    }

    static LocalDate utledStartdato(BeregningsgrunnlagInput input, InntektsmeldingDto im,
                                   Set<YrkesaktivitetDto> yrkesaktiviteter, List<AktivitetsAvtaleDto> ansattperioder,
                                   PermisjonFilter permisjonFilter) {
        var skjæringstidspunkt = input.getSkjæringstidspunktForBeregning();
        var ansettelsesperiode = FinnAnsettelsesPeriode.getMinMaksPeriode(ansattperioder, skjæringstidspunkt);
        var førsteDagEtterPermisjon = FinnFørsteDagEtterPermisjon.finn(yrkesaktiviteter, ansettelsesperiode,
            skjæringstidspunkt, permisjonFilter);
        if (førsteDagEtterPermisjon.isEmpty()) {
            return skjæringstidspunkt;
        }
        if (input.getFagsakYtelseType() == FagsakYtelseType.FORELDREPENGER) {
            return FinnStartdatoPermisjon.finnStartdatoPermisjon(Optional.of(im), skjæringstidspunkt,
                førsteDagEtterPermisjon.get());
        }
        var utbetalingsgradGrunnlag = (UtbetalingsgradGrunnlag) input.getYtelsespesifiktGrunnlag();
        var førsteDatoMedUtbetaling = yrkesaktiviteter.stream()
            .map(ya -> utbetalingsgradGrunnlag.finnUtbetalingsgraderForArbeid(ya.getArbeidsgiver(), ya.getArbeidsforholdRef()))
            .flatMap(Collection::stream)
            .filter(p -> p.getUtbetalingsgrad().compareTo(Utbetalingsgrad.ZERO) != 0)
            .map(PeriodeMedUtbetalingsgradDto::getPeriode)
            .map(Intervall::getFomDato)
            .min(Comparator.naturalOrder());
        return førsteDatoMedUtbetaling
            .map(dato -> førsteDagEtterPermisjon.get().isAfter(dato) ? førsteDagEtterPermisjon.get() : dato)
            .map(dato -> skjæringstidspunkt.isAfter(dato) ? skjæringstidspunkt : dato)
            .orElse(skjæringstidspunkt);
    }

    static List<Intervall> finnGyldigePerioder(BeregningsgrunnlagInput input, InntektsmeldingDto im,
                                              LocalDate startdato, List<AktivitetsAvtaleDto> ansattperioder) {
        if (im.getRefusjonOpphører() != null && im.getRefusjonOpphører().isBefore(startdato)) {
            return List.of();
        }
        if (input.getFagsakYtelseType() == FagsakYtelseType.FORELDREPENGER) {
            return List.of(Intervall.fraOgMedTilOgMed(startdato, TIDENES_ENDE));
        }
        if (!ansattperioder.isEmpty() && ansattperioder.stream().allMatch(a -> a.getPeriode().getTomDato().isBefore(startdato))) {
            return List.of();
        }
        if (!(input.getYtelsespesifiktGrunnlag() instanceof UtbetalingsgradGrunnlag grunnlag)) {
            throw new IllegalStateException("Forventet utbetalingsgrader men fant ikke UtbetalingsgradGrunnlag.");
        }
        var fraværHosArbeidsgiver = UtbetalingsgradTjeneste.finnPerioderForArbeid(grunnlag,
                im.getArbeidsgiver(), im.getArbeidsforholdRef(), false).stream()
            .flatMap(u -> u.getPeriodeMedUtbetalingsgrad().stream())
            .filter(p -> harUtbetalingsgrad(p)
                || p.getAktivitetsgrad().map(ag -> ag.compareTo(Aktivitetsgrad.HUNDRE) < 0).orElse(false))
            .map(PeriodeMedUtbetalingsgradDto::getPeriode)
            .map(p -> new LocalDateTimeline<>(p.getFomDato(), p.getTomDato(), true))
            .reduce(LocalDateTimeline::crossJoin).orElse(LocalDateTimeline.empty());
        var utbetalingForYtelse = grunnlag.getUtbetalingsgradPrAktivitet().stream()
            .flatMap(u -> u.getPeriodeMedUtbetalingsgrad().stream())
            .filter(RefusjonsgrunnlagGyldigePerioder::harUtbetalingsgrad)
            .map(PeriodeMedUtbetalingsgradDto::getPeriode)
            .map(p -> new LocalDateTimeline<>(p.getFomDato(), p.getTomDato(), true))
            .reduce(LocalDateTimeline::crossJoin).orElse(LocalDateTimeline.empty());
        return fraværHosArbeidsgiver.intersection(utbetalingForYtelse).compress().getLocalDateIntervals().stream()
            .map(p -> Intervall.fraOgMedTilOgMed(p.getFomDato(), p.getTomDato()))
            .toList();
    }

    private static boolean harUtbetalingsgrad(PeriodeMedUtbetalingsgradDto periode) {
        return periode.getUtbetalingsgrad() != null && periode.getUtbetalingsgrad().compareTo(Utbetalingsgrad.ZERO) > 0;
    }
}
