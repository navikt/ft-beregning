package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;


import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Refusjonskrav;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektsmeldingDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.RefusjonDto;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public class MapRefusjonskravFraVLTilRegel {

    /**
     * Vi utbetaler kun refusjon dersom det utgjør en dagsats på 50 øre eller mer, dvs et årsbeløp på 130 kroner eller månedsbeløp på 10 kroner og 83 øre
     */
    public static final BigDecimal MINSTE_UTBETALTE_REFUSJONSKRAV_PR_MÅNED = BigDecimal.valueOf(10.83);

    private MapRefusjonskravFraVLTilRegel() {
        // skjul public constructor
    }

    public static List<Refusjonskrav> periodiserRefusjonsbeløp(InntektsmeldingDto inntektsmelding,
                                                               LocalDate startdatoPermisjon,
                                                               Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer,
                                                               List<Intervall> gyldigeRefusjonPerioder) {

        if (gyldigeRefusjonPerioder.isEmpty()) {
            return Collections.emptyList();
        } else {
            LocalDateTimeline<BigDecimal> timeline = lagInntektsmeldingTidslinje(inntektsmelding, startdatoPermisjon, refusjonOverstyringer, gyldigeRefusjonPerioder);
            timeline = velgGyldigePerioder(gyldigeRefusjonPerioder, timeline);
            timeline = fyllMellomromMedNull(startdatoPermisjon, timeline);
            return timeline.compress()
                    .stream()
                    .map(segment -> new Refusjonskrav(segment.getValue(), segment.getFom(), segment.getTom()))
                    .collect(Collectors.toList());
        }
    }

    private static LocalDateTimeline<BigDecimal> velgGyldigePerioder(List<Intervall> gyldigeRefusjonPerioder, LocalDateTimeline<BigDecimal> timeline) {
        return timeline.intersection(lagGyldigTidslinje(gyldigeRefusjonPerioder));
    }

    private static LocalDateTimeline<BigDecimal> fyllMellomromMedNull(LocalDate startdatoPermisjon, LocalDateTimeline<BigDecimal> timeline) {
        return timeline.combine(new LocalDateSegment<>(startdatoPermisjon, TIDENES_ENDE, BigDecimal.ZERO),
                StandardCombinators::sum, LocalDateTimeline.JoinStyle.RIGHT_JOIN);
    }

    private static LocalDateTimeline<BigDecimal> lagInntektsmeldingTidslinje(InntektsmeldingDto inntektsmelding, LocalDate startdatoPermisjon, Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer, List<Intervall> gyldigeRefusjonPerioder) {
        Map<LocalDate, Beløp> refusjonFraInntektsmelding = finnRefusjonendringFraInntektsmelding(inntektsmelding, startdatoPermisjon, refusjonOverstyringer, gyldigeRefusjonPerioder);
        List<Refusjonskrav> refusjonskravListeFraInntektsmelding = lagForenkletRefusjonListe(refusjonFraInntektsmelding);
        List<LocalDateSegment<BigDecimal>> kravFraInntektsmelding = refusjonskravListeFraInntektsmelding.stream()
                .map(krav -> new LocalDateSegment<>(krav.getPeriode().getFom(), krav.getPeriode().getTom(), krav.getMånedsbeløp()))
                .collect(Collectors.toList());
        return new LocalDateTimeline<>(kravFraInntektsmelding);
    }

    private static LocalDateTimeline<Object> lagGyldigTidslinje(List<Intervall> gyldigeRefusjonPerioder) {
        List<LocalDateSegment<Object>> gyldigeSegmenter = gyldigeRefusjonPerioder.stream()
                .map(p -> LocalDateSegment.emptySegment(p.getFomDato(), p.getTomDato()))
                .collect(Collectors.toList());
        return new LocalDateTimeline<>(gyldigeSegmenter);
    }


    private static Map<LocalDate, Beløp> finnRefusjonendringFraInntektsmelding(InntektsmeldingDto inntektsmelding,
                                                                               LocalDate startdatoPermisjon,
                                                                               Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer,
                                                                               List<Intervall> gyldigeRefusjonPerioder) {
        Map<LocalDate, Beløp> refusjoner = new TreeMap<>();
        var listeMedOverstyringer = refusjonOverstyringer.map(BeregningRefusjonOverstyringerDto::getRefusjonOverstyringer).orElse(Collections.emptyList());
        var refusjonBeløpPerMnd = Optional.ofNullable(inntektsmelding.getRefusjonBeløpPerMnd()).orElse(Beløp.ZERO);

        var refusjonoverstyringForIM = finnOverstyringForInntektsmelding(inntektsmelding, listeMedOverstyringer);
        var overstyrtStartdato = refusjonoverstyringForIM.map(BeregningRefusjonPeriodeDto::getStartdatoRefusjon);
        var gjeldendeStartdato = overstyrtStartdato.orElse(startdatoPermisjon);
        refusjoner.put(gjeldendeStartdato, refusjonBeløpPerMnd);
        inntektsmelding.getEndringerRefusjon()
                .stream()
                .sorted(Comparator.comparing(RefusjonDto::getFom))
                .forEach(endring -> {
                    LocalDate fom = finnRefusjonFomDato(gyldigeRefusjonPerioder, endring);
                    if (fom.isBefore(gjeldendeStartdato)) {
                        refusjoner.put(gjeldendeStartdato, endring.getRefusjonsbeløp());
                    } else if (fom.isBefore(TIDENES_ENDE)) {
                        refusjoner.put(fom, endring.getRefusjonsbeløp());
                    }
                });
        if (inntektsmelding.getRefusjonOpphører() != null && !TIDENES_ENDE.equals(inntektsmelding.getRefusjonOpphører())) {
            refusjoner.put(inntektsmelding.getRefusjonOpphører().plusDays(1), Beløp.ZERO);
        }
        return refusjoner;
    }

    private static LocalDate finnRefusjonFomDato(List<Intervall> gyldigeRefusjonPerioder, RefusjonDto endring) {
        LocalDate fom;
        if (gyldigeRefusjonPerioder.stream().anyMatch(p -> p.inkluderer(endring.getFom()))) {
            fom = endring.getFom();
        } else {
            fom = gyldigeRefusjonPerioder.stream()
                    .filter(p -> p.getFomDato().isAfter(endring.getFom()))
                    .min(Comparator.comparing(Intervall::getFomDato))
                    .map(Intervall::getFomDato)
                    .orElse(TIDENES_ENDE);
        }
        return fom;
    }

    private static Optional<BeregningRefusjonPeriodeDto> finnOverstyringForInntektsmelding(InntektsmeldingDto inntektsmelding, List<BeregningRefusjonOverstyringDto> refusjonOverstyringer) {
        List<BeregningRefusjonPeriodeDto> overstyringerForAG = refusjonOverstyringer.stream()
                .filter(ro -> ro.getArbeidsgiver().equals(inntektsmelding.getArbeidsgiver()))
                .findFirst()
                .map(BeregningRefusjonOverstyringDto::getRefusjonPerioder)
                .orElse(Collections.emptyList());
        return overstyringerForAG.stream()
                .filter(periode -> matcherReferanse(periode.getArbeidsforholdRef(), inntektsmelding.getArbeidsforholdRef()))
                .findFirst();
    }

    private static boolean matcherReferanse(InternArbeidsforholdRefDto refFraOverstyring, InternArbeidsforholdRefDto refFraIM) {
        String ref1 = refFraOverstyring.getReferanse();
        String ref2 = refFraIM.getReferanse();
        return Objects.equals(ref1, ref2);
    }

    static List<Refusjonskrav> periodiserGradertRefusjonsbeløp(InntektsmeldingDto inntektsmelding,
                                                               List<PeriodeMedUtbetalingsgradDto> utbetalingsgrader,
                                                               LocalDate stp) {
        Map<LocalDate, Beløp> refusjoner = new TreeMap<>();
        Beløp refusjonBeløpPerMnd = Optional.ofNullable(inntektsmelding.getRefusjonBeløpPerMnd()).orElse(Beløp.ZERO);
        Optional<PeriodeMedUtbetalingsgradDto> førsteUtbetalingsperiode = finnFørsteUtbetalingsgradPeriode(utbetalingsgrader, stp);
        BigDecimal utbetalingsgradVedStart = førsteUtbetalingsperiode.map(PeriodeMedUtbetalingsgradDto::getUtbetalingsgrad)
                .map(g -> g.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN)).orElse(BigDecimal.ZERO);
        LocalDate startdatoPermisjon = førsteUtbetalingsperiode.map(PeriodeMedUtbetalingsgradDto::getPeriode).map(Intervall::getFomDato).orElse(TIDENES_ENDE);
        refusjoner.put(startdatoPermisjon, refusjonBeløpPerMnd.multipliser(utbetalingsgradVedStart));
        inntektsmelding.getEndringerRefusjon()
                .stream()
                .sorted(Comparator.comparing(RefusjonDto::getFom))
                .forEach(endring -> {
                    if (endring.getFom().isBefore(startdatoPermisjon)) {
                        refusjoner.put(startdatoPermisjon, endring.getRefusjonsbeløp().multipliser(utbetalingsgradVedStart));
                    } else {
                        BigDecimal utbetalingsgrad = finnUtbetalingsgradForDato(utbetalingsgrader, startdatoPermisjon);
                        refusjoner.put(endring.getFom(), endring.getRefusjonsbeløp().multipliser(utbetalingsgrad));
                    }
                });

        if (inntektsmelding.getRefusjonOpphører() != null && !TIDENES_ENDE.equals(inntektsmelding.getRefusjonOpphører())) {
            refusjoner.put(inntektsmelding.getRefusjonOpphører().plusDays(1), Beløp.ZERO);
        }
        return lagForenkletRefusjonListe(refusjoner);
    }

    private static BigDecimal finnUtbetalingsgradForDato(List<PeriodeMedUtbetalingsgradDto> utbetalingsgrader, LocalDate startdatoPermisjon) {
        return utbetalingsgrader.stream().filter(u -> u.getPeriode().inkluderer(startdatoPermisjon))
                .map(PeriodeMedUtbetalingsgradDto::getUtbetalingsgrad)
                .map(b -> b.verdi().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_EVEN))
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private static Optional<PeriodeMedUtbetalingsgradDto> finnFørsteUtbetalingsgradPeriode(List<PeriodeMedUtbetalingsgradDto> utbetalingsgrader, LocalDate stp) {
        return utbetalingsgrader.stream()
                .filter(ugr -> ugr.getPeriode().inkluderer(stp))
                .min(Comparator.comparing(u -> u.getPeriode().getFomDato()));

    }

    private static List<Refusjonskrav> lagForenkletRefusjonListe(Map<LocalDate, Beløp> refusjoner) {
        List<Refusjonskrav> refusjonskravListe = new ArrayList<>();
        List<Map.Entry<LocalDate, Beløp>> entryList = new ArrayList<>(refusjoner.entrySet());
        ListIterator<Map.Entry<LocalDate, Beløp>> listIterator = entryList.listIterator();

        while (listIterator.hasNext()) {
            Map.Entry<LocalDate, Beløp> entry = listIterator.next();
            LocalDate fom = entry.getKey();
            LocalDate tom = utledTom(entryList, listIterator);
            BigDecimal refusjonPrMåned = entry.getValue().verdi();
            // Mapper kun refusjonskrav som fører til utbetaling
            if (refusjonPrMåned.compareTo(MINSTE_UTBETALTE_REFUSJONSKRAV_PR_MÅNED) > 0) {
                refusjonskravListe.add(new Refusjonskrav(refusjonPrMåned, fom, tom));
            } else {
                refusjonskravListe.add(new Refusjonskrav(BigDecimal.ZERO, fom, tom));
            }
        }
        return refusjonskravListe;
    }

    private static LocalDate utledTom(List<Map.Entry<LocalDate, Beløp>> entryList, ListIterator<Map.Entry<LocalDate, Beløp>> listIterator) {
        Optional<LocalDate> nesteFomOpt = hentNesteFom(entryList, listIterator);
        return nesteFomOpt.map(nesteFom -> nesteFom.minusDays(1)).orElse(null);
    }

    private static Optional<LocalDate> hentNesteFom(List<Map.Entry<LocalDate, Beløp>> entryList,
                                                    ListIterator<Map.Entry<LocalDate, Beløp>> listIterator) {
        if (listIterator.hasNext()) {
            Map.Entry<LocalDate, Beløp> nesteEntry = entryList.get(listIterator.nextIndex());
            return Optional.of(nesteEntry.getKey());
        }
        return Optional.empty();
    }

    public static Beløp finnGradertRefusjonskravPåSkjæringstidspunktet(Collection<InntektsmeldingDto> inntektsmeldingerSomSkalBrukes,
                                                                            LocalDate stp,
                                                                            YtelsespesifiktGrunnlag ytelsespesifiktGrunnlag) {
        List<Refusjonskrav> refusjonskravs = new ArrayList<>();
        for (InntektsmeldingDto inntektsmeldingerSomSkalBruke : inntektsmeldingerSomSkalBrukes) {
            if (ytelsespesifiktGrunnlag instanceof UtbetalingsgradGrunnlag) {
                UtbetalingsgradGrunnlag utbetalingsgradGrunnlag = (UtbetalingsgradGrunnlag) ytelsespesifiktGrunnlag;
                var utbetalingsgrader = utbetalingsgradGrunnlag.finnUtbetalingsgraderForArbeid(inntektsmeldingerSomSkalBruke.getArbeidsgiver(), inntektsmeldingerSomSkalBruke.getArbeidsforholdRef());
                refusjonskravs.addAll(MapRefusjonskravFraVLTilRegel.periodiserGradertRefusjonsbeløp(inntektsmeldingerSomSkalBruke, utbetalingsgrader, stp));
            } else {
                throw new IllegalStateException("Må ha utbetalingsgradgrunnlag for å beregne gradert refusjonskrav");
            }
        }
        List<Refusjonskrav> relevanteRefusjonskrav = refusjonskravs.stream()
                .filter(p -> p.getPeriode().inneholder(stp))
                .collect(Collectors.toList());
        var fraRegel = relevanteRefusjonskrav.stream()
                .map(Refusjonskrav::getMånedsbeløp)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO)
                .multiply(KonfigTjeneste.getMånederIÅr());
        return Beløp.fra(fraRegel);
    }

}
