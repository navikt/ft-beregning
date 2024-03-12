package no.nav.folketrygdloven.kalkulator.steg.tilkommetInntekt;

import static java.lang.Boolean.TRUE;
import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.inntektgradering.DagsatsPrKategoriOgArbeidsgiver;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.UtbetalingsgradPrAktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

class UtvidetInntektsperiodeUtleder {

    private static final int MAKS_ANTALL_MÅNEDER_UTEN_INNTEKT = 2;


    /**
     * Lager tidslinje over periode der arbeidsfohold fra en arbeidsgiver anses å vere aktivt sett i kontekst av inntekt.
     *
     * @param registerInntektTidslinje     tidslinje for inntekt
     * @param utbetalingsgradGrunnlag      Grunnlag for utbetalingsgrad
     * @param inntektRapporteringsfristDag Dag for frist for innrapportering av inntekt
     * @return Utvidet periode for inntekt
     */
    public static LocalDateTimeline<Set<Arbeidsgiver>> lagGodkjenteInntektsperiodeTidslinje(LocalDateTimeline<Set<DagsatsPrKategoriOgArbeidsgiver>> registerInntektTidslinje,
                                                                                            UtbetalingsgradGrunnlag utbetalingsgradGrunnlag,
                                                                                            int inntektRapporteringsfristDag) {
        var godkjenteTidslinjerPrArbeidsgiver = lagGodkjentInntektsperiodePrArbeidsgiver(utbetalingsgradGrunnlag, inntektRapporteringsfristDag, registerInntektTidslinje);
        return unionAvTidslinjer(godkjenteTidslinjerPrArbeidsgiver);
    }

    private static <V> LocalDateTimeline<Set<V>> unionAvTidslinjer(List<LocalDateTimeline<V>> godkjenteTidslinjerPrArbeidsgiver) {
        return godkjenteTidslinjerPrArbeidsgiver.stream()
                .map(t -> t.filterValue(Objects::nonNull).mapValue(Set::of)) // For å kunne bruke union
                .reduce(LocalDateTimeline.empty(), (tl1, tl2) -> tl1.combine(tl2, StandardCombinators::union, LocalDateTimeline.JoinStyle.CROSS_JOIN));
    }

    private static List<LocalDateTimeline<Arbeidsgiver>> lagGodkjentInntektsperiodePrArbeidsgiver(UtbetalingsgradGrunnlag utbetalingsgradGrunnlag,
                                                                                                  int inntektRapporteringsfristDag,
                                                                                                  LocalDateTimeline<Set<DagsatsPrKategoriOgArbeidsgiver>> registerInntektTidslinje) {
        var arbeidsgivere = new HashSet<>(getArbeidsgivereFraInntekttidslinje(registerInntektTidslinje));
        arbeidsgivere.addAll(getArbeidsgivereFraUtbetalingsgrader(utbetalingsgradGrunnlag));
        return arbeidsgivere.stream()
                .map(arbeidsgiver -> {
                    var inntektTidslinje = finnTidslinjeForArbeidsgiver(registerInntektTidslinje, arbeidsgiver);
                    var utbetalingsgradPrAktivitetDto = finnUtbetalingsgrader(utbetalingsgradGrunnlag, arbeidsgiver);
                    var tidslinjeForGodkjentePerioderUtenInntekt = lagGodkjentePerioderUtenInntekt(utbetalingsgradPrAktivitetDto, inntektRapporteringsfristDag, inntektTidslinje);
                    return tidslinjeForGodkjentePerioderUtenInntekt.mapValue(v -> v ? arbeidsgiver : null);
                })
                .toList();
    }

    private static HashSet<Arbeidsgiver> getArbeidsgivereFraInntekttidslinje(LocalDateTimeline<Set<DagsatsPrKategoriOgArbeidsgiver>> registerInntektTidslinje) {
        return registerInntektTidslinje.toSegments().stream()
                .flatMap(s -> s.getValue().stream().map(DagsatsPrKategoriOgArbeidsgiver::arbeidsgiver))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static List<Arbeidsgiver> getArbeidsgivereFraUtbetalingsgrader(UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        return utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet().stream()
                .map(UtbetalingsgradPrAktivitetDto::getUtbetalingsgradArbeidsforhold)
                .map(AktivitetDto::getArbeidsgiver)
                .flatMap(Optional::stream)
                .distinct()
                .toList();
    }

    private static LocalDateTimeline<Beløp> finnTidslinjeForArbeidsgiver(LocalDateTimeline<Set<DagsatsPrKategoriOgArbeidsgiver>> registerInntektTidslinje, Arbeidsgiver arbeidsgiver) {
        return registerInntektTidslinje.mapValue(v -> v.stream().filter(i -> Objects.equals(i.arbeidsgiver(), arbeidsgiver)).findFirst()
                        .map(DagsatsPrKategoriOgArbeidsgiver::dagsats).orElse(null))
                .filterValue(Objects::nonNull);
    }

    private static List<UtbetalingsgradPrAktivitetDto> finnUtbetalingsgrader(UtbetalingsgradGrunnlag utbetalingsgradGrunnlag, Arbeidsgiver arbeidsgiver) {
        return UtbetalingsgradTjeneste.finnPerioderForArbeid(utbetalingsgradGrunnlag, arbeidsgiver, InternArbeidsforholdRefDto.nullRef(), false);
    }

    private static LocalDateTimeline<Boolean> lagGodkjentePerioderUtenInntekt(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitetDto, int inntektRapporteringsfristDag, LocalDateTimeline<Beløp> inntektTidslinje) {
        var godkjenteSegmenter = new ArrayList<>(godkjennGrunnetFulltFravær(utbetalingsgradPrAktivitetDto));
        if (!inntektTidslinje.isEmpty()) {
            godkjenteSegmenter.addAll(godkjennGrunnetKortereOppholdAvInntekt(inntektTidslinje));
            godkjenteSegmenter.add(godkjennGrunnetIkkePassertFrist(inntektTidslinje, inntektRapporteringsfristDag));
        }
        return new LocalDateTimeline<>(godkjenteSegmenter, (di, lhs, rhs) -> new LocalDateSegment<>(di, lhs.getValue() || rhs.getValue()));
    }

    private static List<LocalDateSegment<Boolean>> godkjennGrunnetKortereOppholdAvInntekt(LocalDateTimeline<Beløp> inntektTidslinje) {
        return fyllMellomromFraDato(inntektTidslinje.getMinLocalDate(), inntektTidslinje).compress((e1, e2) -> e1.compareTo(e2) == 0, StandardCombinators::leftOnly).toSegments().stream()
                .map(UtvidetInntektsperiodeUtleder::godkjennInntektEllerHullPåMindreEnnTreMåneder)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static LocalDateTimeline<Beløp> fyllMellomromFraDato(LocalDate fomDato, LocalDateTimeline<Beløp> inntektPerioderTidslinje) {
        var mellomrom = new LocalDateTimeline<>(new LocalDateInterval(fomDato, LocalDateInterval.TIDENES_ENDE), Beløp.ZERO);
        return inntektPerioderTidslinje.combine(mellomrom, UtvidetInntektsperiodeUtleder::summer, LocalDateTimeline.JoinStyle.CROSS_JOIN).compress();
    }

    public static LocalDateSegment<Beløp> summer(LocalDateInterval dateInterval, LocalDateSegment<Beløp> lhs, LocalDateSegment<Beløp> rhs) {
        return new LocalDateSegment<>(dateInterval, Beløp.safeSum(lhs == null ? null : lhs.getValue(), rhs == null ? null : rhs.getValue()));
    }

    private static List<LocalDateSegment<Boolean>> godkjennGrunnetFulltFravær(List<UtbetalingsgradPrAktivitetDto> utbetalingsgradPrAktivitetDto) {
        return utbetalingsgradPrAktivitetDto.stream()
                .flatMap(it -> it.getPeriodeMedUtbetalingsgrad().stream())
                .filter(p -> p.getAktivitetsgrad().map(ag -> ag.compareTo(Aktivitetsgrad.ZERO) == 0).orElse(false))
                .map(PeriodeMedUtbetalingsgradDto::getPeriode)
                .map(it -> new LocalDateSegment<>(it.getFomDato(), it.getTomDato(), TRUE))
                .toList();
    }

    private static LocalDateSegment<Boolean> godkjennInntektEllerHullPåMindreEnnTreMåneder(LocalDateSegment<Beløp> s) {
        if (s.getValue().compareTo(Beløp.ZERO) > 0) {
            return new LocalDateSegment<>(s.getFom(), s.getTom(), TRUE);
        } else {
            var skalHulletGodkjennes = skalAnsesSomAktivIPeriodeUtenUtbetaling(s.getFom(), s.getTom());
            return new LocalDateSegment<>(s.getFom(), s.getTom(), skalHulletGodkjennes);
        }
    }

    private static Boolean skalAnsesSomAktivIPeriodeUtenUtbetaling(LocalDate fom, LocalDate tom) {
        var fomJustert = førsteIMånedenFramITid(fom);
        var tomJustert = sisteIMånedenTilbakeITid(tom);
        if (!fomJustert.isBefore(tom)) {
            return true;
        }
        var period = fomJustert.until(tomJustert.plusDays(1));
        var antallMåneder = period.getMonths() + period.getYears() * KonfigTjeneste.getMånederIÅr().intValue();
        return antallMåneder <= MAKS_ANTALL_MÅNEDER_UTEN_INNTEKT;
    }

    private static LocalDate førsteIMånedenFramITid(LocalDate dato) {
        if (dato.getDayOfMonth() == 1) {
            return dato;
        }
        return dato.plusMonths(1).withDayOfMonth(1);
    }

    private static LocalDate sisteIMånedenTilbakeITid(LocalDate dato) {
        if (dato == dato.with(TemporalAdjusters.lastDayOfMonth())) {
            return dato;
        }
        return dato.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
    }

    private static LocalDateSegment<Boolean> godkjennGrunnetIkkePassertFrist(LocalDateTimeline<Beløp> inntektTidslinje, int inntektRapporteringsfristDag) {
        var førsteMånedUtenPassertFrist = finnFørsteMånedUtenPassertRapporteringsfrist(inntektRapporteringsfristDag);
        var overlappendeInntektSegment = fyllMellomromFraDato(inntektTidslinje.getMinLocalDate(), inntektTidslinje).toSegments()
                .stream().filter(s -> s.getLocalDateInterval().contains(førsteMånedUtenPassertFrist))
                .findFirst().orElseThrow();
        if (overlappendeInntektSegment.getFom().isEqual(førsteMånedUtenPassertFrist)) {
            return new LocalDateSegment<>(overlappendeInntektSegment.getFom(), TIDENES_ENDE, TRUE);
        }
        var segmentTilFørsteIkkeRapporterteMåned = new LocalDateSegment<>(overlappendeInntektSegment.getFom(), førsteMånedUtenPassertFrist.minusDays(1), overlappendeInntektSegment.getValue());
        var godkjentSegmentFramTilFrist = godkjennInntektEllerHullPåMindreEnnTreMåneder(segmentTilFørsteIkkeRapporterteMåned);
        if (godkjentSegmentFramTilFrist.getValue()) {
            return new LocalDateSegment<>(godkjentSegmentFramTilFrist.getFom(), TIDENES_ENDE, TRUE);
        }
        return new LocalDateSegment<>(førsteMånedUtenPassertFrist, TIDENES_ENDE, TRUE);
    }

    private static LocalDate finnFørsteMånedUtenPassertRapporteringsfrist(int inntektRapporteringsfristDag) {
        var iDag = LocalDate.now();
        var inntektRapporteringsdato = iDag.withDayOfMonth(inntektRapporteringsfristDag);
        if (iDag.isBefore(inntektRapporteringsdato)) {
            return iDag.minusMonths(1).withDayOfMonth(1);
        } else {
            return iDag.withDayOfMonth(1);
        }
    }

}
