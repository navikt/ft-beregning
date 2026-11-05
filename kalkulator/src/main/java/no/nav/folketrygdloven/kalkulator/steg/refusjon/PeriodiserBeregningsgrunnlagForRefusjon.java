package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering.FinnAnsettelsesPeriode;
import no.nav.folketrygdloven.kalkulator.felles.FinnYrkesaktiviteterForBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BGAndelArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.RefusjonsandelDto;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.RefusjonsperiodeDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public final class PeriodiserBeregningsgrunnlagForRefusjon {

    private PeriodiserBeregningsgrunnlagForRefusjon() {
    }

    public static BeregningsgrunnlagDto periodiser(BeregningsgrunnlagInput input, FordelingGrunnlagDto fordelingGrunnlag) {
        var beregningsgrunnlag = input.getBeregningsgrunnlag();
        var tidslinjer = new LinkedHashMap<RefusjonsandelDto, LocalDateTimeline<RefusjonsperiodeDto>>();
        var årsaker = new TreeMap<LocalDate, List<PeriodeÅrsak>>();
        for (var andel : fordelingGrunnlag.refusjonsandeler()) {
            var tidslinje = new LocalDateTimeline<>(andel.refusjonsperioder().stream()
                .map(p -> {
                    mapUtfall(p);
                    if (p.refusjonPrMnd() == null || p.refusjonPrMnd().compareTo(Beløp.ZERO) < 0) {
                        throw new IllegalArgumentException("Refusjonsperioden må ha et ikke-negativt månedsbeløp.");
                    }
                    return new LocalDateSegment<>(p.periode().getFomDato(), p.periode().getTomDato(), p);
                }).toList());
            if (tidslinjer.entrySet().stream().anyMatch(e -> e.getKey().arbeidsgiver().equals(andel.arbeidsgiver())
                && e.getKey().arbeidsforholdRef().gjelderFor(andel.arbeidsforholdRef())
                && !e.getValue().intersection(tidslinje).isEmpty())) {
                throw new IllegalArgumentException("Overlappende refusjonsperioder for samme arbeidsforhold.");
            }
            tidslinjer.put(andel, tidslinje);
            var førsteDato = tidslinje.isEmpty() ? null : tidslinje.getMinLocalDate();
            for (var segment : tidslinje.segmenter()) {
                var p = segment.getValue();
                if (!(segment.getFom().equals(førsteDato) && p.refusjonPrMnd().erNullEller0())) {
                    årsaker.computeIfAbsent(segment.getFom(), d -> new ArrayList<>()).add(utledÅrsak(p));
                }
                if (segment.getTom().isBefore(TIDENES_ENDE)
                    && tidslinje.getSegment(new LocalDateInterval(segment.getTom().plusDays(1), segment.getTom().plusDays(1))) == null) {
                    årsaker.computeIfAbsent(segment.getTom().plusDays(1), d -> new ArrayList<>()).add(PeriodeÅrsak.REFUSJON_OPPHØRER);
                }
            }
        }
        var nytt = BeregningsgrunnlagDto.builder(beregningsgrunnlag).fjernAllePerioder().build();
        for (var original : beregningsgrunnlag.getBeregningsgrunnlagPerioder()) {
            var matcher = finnMatchendeAndeler(original, tidslinjer);
            var grenser = new TreeSet<LocalDate>();
            grenser.add(original.getBeregningsgrunnlagPeriodeFom());
            årsaker.keySet().stream().filter(original.getPeriode()::inkluderer).forEach(grenser::add);
            var datoer = new ArrayList<>(grenser);
            for (int i = 0; i < datoer.size(); i++) {
                var fom = datoer.get(i);
                var tom = i + 1 < datoer.size() ? datoer.get(i + 1).minusDays(1) : original.getBeregningsgrunnlagPeriodeTom();
                var periode = BeregningsgrunnlagPeriodeDto.kopier(original).fjernAlleBeregningsgrunnlagPrStatusOgAndeler()
                    .medBeregningsgrunnlagPeriode(fom, tom);
                if (!fom.equals(beregningsgrunnlag.getSkjæringstidspunkt())) {
                    periode.leggTilPeriodeÅrsaker(årsaker.getOrDefault(fom, List.of()));
                }
                for (var andel : original.getBeregningsgrunnlagPrStatusOgAndelList()) {
                    var kopi = BeregningsgrunnlagPrStatusOgAndelDto.kopier(andel);
                    var matchende = matcher.getOrDefault(andel.getAndelsnr(), List.of());
                    if (!matchende.isEmpty()) {
                        var krav = finnKrav(matchende, tidslinjer, fom);
                        var af = kopi.getBgAndelArbeidsforholdDtoBuilder();
                        af.medRefusjon(lagRefusjon(krav, andel.getBgAndelArbeidsforhold().orElseThrow().getRefusjon()));
                        kopi.medBGAndelArbeidsforhold(af);
                    }
                    periode.leggTilBeregningsgrunnlagPrStatusOgAndel(kopi);
                }
                for (var refusjonsandel : tidslinjer.keySet()) {
                    if (matcher.values().stream().anyMatch(l -> l.contains(refusjonsandel))) {
                        continue;
                    }
                    var krav = finnKrav(List.of(refusjonsandel), tidslinjer, fom);
                    if (krav != null && krav.refusjonPrMnd().compareTo(Beløp.ZERO) > 0) {
                        var arbeidsperiode = finnArbeidsperiode(input, refusjonsandel);
                        if (arbeidsperiode.filter(p -> p.getTom().isBefore(beregningsgrunnlag.getSkjæringstidspunkt())).isPresent()) {
                            continue;
                        }
                        var af = BGAndelArbeidsforholdDto.builder().medArbeidsgiver(refusjonsandel.arbeidsgiver())
                            .medArbeidsforholdRef(refusjonsandel.arbeidsforholdRef())
                            .medRefusjon(lagRefusjon(krav, Optional.empty()));
                        arbeidsperiode.ifPresent(p -> af.medArbeidsperiodeFom(p.getFom()).medArbeidsperiodeTom(p.getTom()));
                        periode.leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto.ny()
                            .medBGAndelArbeidsforhold(af).medKilde(AndelKilde.PROSESS_PERIODISERING)
                            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medArbforholdType(OpptjeningAktivitetType.ARBEID));
                    }
                }
                // Andelsbuilderen summerer brutto på nytt. Periodens øvrige beregningsdata skal beholdes.
                periode.medBruttoPrÅr(original.getBruttoPrÅr()).build(nytt);
            }
        }
        return nytt;
    }

    private static Optional<Periode> finnArbeidsperiode(BeregningsgrunnlagInput input, RefusjonsandelDto andel) {
        var skjæringstidspunkt = input.getSkjæringstidspunktForBeregning();
        var ansettelsesperioder = FinnYrkesaktiviteterForBeregningTjeneste.finnYrkesaktiviteter(
                input.getIayGrunnlag(), input.getBeregningsgrunnlagGrunnlag(), skjæringstidspunkt).stream()
            .filter(ya -> ya.gjelderFor(andel.arbeidsgiver(), andel.arbeidsforholdRef()))
            .flatMap(ya -> ya.getAlleAnsettelsesperioder().stream())
            .toList();
        return FinnAnsettelsesPeriode.finnMinMaksPeriode(ansettelsesperioder, skjæringstidspunkt);
    }

    private static Map<Long, List<RefusjonsandelDto>> finnMatchendeAndeler(BeregningsgrunnlagPeriodeDto original,
                                                                         Map<RefusjonsandelDto, ?> tidslinjer) {
        var matcher = new HashMap<Long, List<RefusjonsandelDto>>();
        for (var refusjonsandel : tidslinjer.keySet()) {
            // Som den gamle inputmapperen: uspesifisert IM-ref treffer første andel, spesifisert ref krever likhet.
            original.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(a -> a.getArbeidsgiver().filter(refusjonsandel.arbeidsgiver()::equals).isPresent())
                .filter(a -> !refusjonsandel.arbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()
                    || a.getArbeidsforholdRef().filter(refusjonsandel.arbeidsforholdRef()::equals).isPresent())
                .findFirst().ifPresent(a -> matcher.computeIfAbsent(a.getAndelsnr(), n -> new ArrayList<>()).add(refusjonsandel));
        }
        return matcher;
    }

    private static RefusjonsperiodeDto finnKrav(List<RefusjonsandelDto> andeler,
                                                Map<RefusjonsandelDto, LocalDateTimeline<RefusjonsperiodeDto>> tidslinjer,
                                                LocalDate dato) {
        var krav = andeler.stream().map(a -> tidslinjer.get(a).getSegment(new LocalDateInterval(dato, dato))).filter(s -> s != null)
            .map(LocalDateSegment::getValue).toList();
        if (krav.size() > 1) {
            throw new IllegalArgumentException("Overlappende refusjonsperioder treffer samme BG-andel.");
        }
        return krav.isEmpty() ? null : krav.getFirst();
    }

    private static Refusjon lagRefusjon(RefusjonsperiodeDto krav, Optional<Refusjon> eksisterende) {
        var beløp = krav == null ? Beløp.ZERO : krav.refusjonPrMnd().multipliser(12);
        var utfall = krav == null ? Utfall.UDEFINERT : mapUtfall(krav);
        var nullstill = beløp.erNullEller0() || utfall == Utfall.UNDERKJENT;
        return new Refusjon(beløp,
            eksisterende.map(Refusjon::getSaksbehandletRefusjonPrÅr).map(b -> nullstill ? Beløp.ZERO : b).orElse(null),
            eksisterende.map(Refusjon::getFordeltRefusjonPrÅr).map(b -> nullstill ? Beløp.ZERO : b).orElse(null),
            eksisterende.map(Refusjon::getManueltFordeltRefusjonPrÅr).map(b -> nullstill ? Beløp.ZERO : b).orElse(null),
            eksisterende.map(Refusjon::getHjemmelForRefusjonskravfrist).orElse(null), utfall);
    }

    private static Utfall mapUtfall(RefusjonsperiodeDto periode) {
        if (periode.utfall() == null) {
            throw new IllegalArgumentException("Refusjonsperioden mangler utfall.");
        }
        return switch (periode.utfall()) {
            case INNVILGET, REDUSERT -> Utfall.GODKJENT;
            case AVSLÅTT -> Utfall.UNDERKJENT;
        };
    }

    private static PeriodeÅrsak utledÅrsak(RefusjonsperiodeDto periode) {
        if (periode.utfall() == RefusjonUtfall.AVSLÅTT) {
            return PeriodeÅrsak.REFUSJON_AVSLÅTT;
        }
        return periode.refusjonPrMnd().erNullEller0() ? PeriodeÅrsak.REFUSJON_OPPHØRER : PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV;
    }
}
