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
        var refusjonstidslinjerPerAndel = lagTidslinjer(fordelingGrunnlag);
        var periodeÅrsakerPerDato = finnPeriodeÅrsaker(refusjonstidslinjerPerAndel);
        var ekisterendeBeregningsgrunnlag = input.getBeregningsgrunnlag();
        var nyttPeriodisertBeregningsgrunnlag = BeregningsgrunnlagDto.builder(ekisterendeBeregningsgrunnlag).fjernAllePerioder().build();

        ekisterendeBeregningsgrunnlag.getBeregningsgrunnlagPerioder()
            .forEach(opprinneligPeriode -> periodiserPeriode(input, nyttPeriodisertBeregningsgrunnlag, opprinneligPeriode,
                refusjonstidslinjerPerAndel, periodeÅrsakerPerDato));
        return nyttPeriodisertBeregningsgrunnlag;
    }

    private static Map<RefusjonsandelDto, LocalDateTimeline<RefusjonsperiodeDto>> lagTidslinjer(FordelingGrunnlagDto fordelingGrunnlag) {
        var refusjonstidslinjerPerAndel = new LinkedHashMap<RefusjonsandelDto, LocalDateTimeline<RefusjonsperiodeDto>>();
        fordelingGrunnlag.refusjonsandeler().forEach(refusjonsandel -> {
            var refusjonstidslinje = lagTidslinje(refusjonsandel);
            if (refusjonstidslinjerPerAndel.entrySet().stream()
                .anyMatch(eksisterende -> eksisterende.getKey().arbeidsgiver().equals(refusjonsandel.arbeidsgiver())
                    && eksisterende.getKey().arbeidsforholdRef().gjelderFor(refusjonsandel.arbeidsforholdRef())
                    && !eksisterende.getValue().intersection(refusjonstidslinje).isEmpty())) {
                throw new IllegalArgumentException("Overlappende refusjonsperioder for samme arbeidsforhold.");
            }
            refusjonstidslinjerPerAndel.put(refusjonsandel, refusjonstidslinje);
        });
        return refusjonstidslinjerPerAndel;
    }

    private static LocalDateTimeline<RefusjonsperiodeDto> lagTidslinje(RefusjonsandelDto refusjonsandel) {
        var refusjonssegmenter = refusjonsandel.refusjonsperioder().stream().map(refusjonsperiode -> {
            mapUtfall(refusjonsperiode);
            if (refusjonsperiode.refusjonPrMnd() == null || refusjonsperiode.refusjonPrMnd().compareTo(Beløp.ZERO) < 0) {
                throw new IllegalArgumentException("Refusjonsperioden må ha et ikke-negativt månedsbeløp.");
            }
            return new LocalDateSegment<>(refusjonsperiode.periode().getFomDato(), refusjonsperiode.periode().getTomDato(), refusjonsperiode);
        }).toList();
        return new LocalDateTimeline<>(refusjonssegmenter);
    }

    private static Map<LocalDate, List<PeriodeÅrsak>> finnPeriodeÅrsaker(
        Map<RefusjonsandelDto, LocalDateTimeline<RefusjonsperiodeDto>> refusjonstidslinjerPerAndel) {
        var periodeÅrsakerPerDato = new TreeMap<LocalDate, List<PeriodeÅrsak>>();
        refusjonstidslinjerPerAndel.values()
            .forEach(refusjonstidslinje -> leggTilPeriodeÅrsaker(refusjonstidslinje, periodeÅrsakerPerDato));
        return periodeÅrsakerPerDato;
    }

    private static void leggTilPeriodeÅrsaker(LocalDateTimeline<RefusjonsperiodeDto> refusjonstidslinje,
                                               Map<LocalDate, List<PeriodeÅrsak>> periodeÅrsakerPerDato) {
        var førsteDatoITidslinjen = refusjonstidslinje.isEmpty() ? null : refusjonstidslinje.getMinLocalDate();
        refusjonstidslinje.segmenter().forEach(refusjonssegment -> {
            var refusjonsperiode = refusjonssegment.getValue();
            if (!(refusjonssegment.getFom().equals(førsteDatoITidslinjen) && refusjonsperiode.refusjonPrMnd().erNullEller0())) {
                periodeÅrsakerPerDato.computeIfAbsent(refusjonssegment.getFom(), dato -> new ArrayList<>()).add(utledÅrsak(refusjonsperiode));
            }
            if (refusjonssegment.getTom().isBefore(TIDENES_ENDE)
                && refusjonstidslinje.getSegment(new LocalDateInterval(refusjonssegment.getTom().plusDays(1),
                    refusjonssegment.getTom().plusDays(1))) == null) {
                periodeÅrsakerPerDato.computeIfAbsent(refusjonssegment.getTom().plusDays(1), dato -> new ArrayList<>())
                    .add(PeriodeÅrsak.REFUSJON_OPPHØRER);
            }
        });
    }

    private static void periodiserPeriode(BeregningsgrunnlagInput input,
                                          BeregningsgrunnlagDto periodisertBeregningsgrunnlag,
                                          BeregningsgrunnlagPeriodeDto opprinneligPeriode,
                                          Map<RefusjonsandelDto, LocalDateTimeline<RefusjonsperiodeDto>> refusjonstidslinjerPerAndel,
                                          Map<LocalDate, List<PeriodeÅrsak>> periodeÅrsakerPerDato) {
        var refusjonsandelerPerAndelsnummer = finnMatchendeAndeler(opprinneligPeriode, refusjonstidslinjerPerAndel);
        var periodestartdatoer = new TreeSet<LocalDate>();
        periodestartdatoer.add(opprinneligPeriode.getBeregningsgrunnlagPeriodeFom());
        periodeÅrsakerPerDato.keySet().stream().filter(opprinneligPeriode.getPeriode()::inkluderer).forEach(periodestartdatoer::add);
        periodestartdatoer.forEach(periodeFom -> {
            var nestePeriodestartdato = periodestartdatoer.higher(periodeFom);
            var periodeTom = nestePeriodestartdato == null
                ? opprinneligPeriode.getBeregningsgrunnlagPeriodeTom() : nestePeriodestartdato.minusDays(1);
            leggTilPeriode(input, periodisertBeregningsgrunnlag, opprinneligPeriode, periodeFom, periodeTom,
                refusjonsandelerPerAndelsnummer, refusjonstidslinjerPerAndel, periodeÅrsakerPerDato);
        });
    }

    private static void leggTilPeriode(BeregningsgrunnlagInput input, BeregningsgrunnlagDto periodisertBeregningsgrunnlag,
                                       BeregningsgrunnlagPeriodeDto opprinneligPeriode, LocalDate periodeFom, LocalDate periodeTom,
                                       Map<Long, List<RefusjonsandelDto>> refusjonsandelerPerAndelsnummer,
                                       Map<RefusjonsandelDto, LocalDateTimeline<RefusjonsperiodeDto>> refusjonstidslinjerPerAndel,
                                       Map<LocalDate, List<PeriodeÅrsak>> periodeÅrsakerPerDato) {
        var periodeBuilder = BeregningsgrunnlagPeriodeDto.kopier(opprinneligPeriode).fjernAlleBeregningsgrunnlagPrStatusOgAndeler()
            .medBeregningsgrunnlagPeriode(periodeFom, periodeTom);
        if (!periodeFom.equals(periodisertBeregningsgrunnlag.getSkjæringstidspunkt())) {
            periodeBuilder.leggTilPeriodeÅrsaker(periodeÅrsakerPerDato.getOrDefault(periodeFom, List.of()));
        }
        opprinneligPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .map(bgAndel -> kopierAndelMedRefusjon(bgAndel, refusjonsandelerPerAndelsnummer, refusjonstidslinjerPerAndel, periodeFom))
            .forEach(periodeBuilder::leggTilBeregningsgrunnlagPrStatusOgAndel);
        refusjonstidslinjerPerAndel.keySet().stream()
            .filter(refusjonsandel -> refusjonsandelerPerAndelsnummer.values().stream()
                .noneMatch(matchendeAndeler -> matchendeAndeler.contains(refusjonsandel)))
            .forEach(refusjonsandel -> leggTilNyAndel(input, periodeBuilder, refusjonsandel, refusjonstidslinjerPerAndel, periodeFom));

        // Andelsbuilderen summerer brutto på nytt. Periodens øvrige beregningsdata skal beholdes.
        periodeBuilder.medBruttoPrÅr(opprinneligPeriode.getBruttoPrÅr()).build(periodisertBeregningsgrunnlag);
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto.Builder kopierAndelMedRefusjon(
        BeregningsgrunnlagPrStatusOgAndelDto bgAndel, Map<Long, List<RefusjonsandelDto>> refusjonsandelerPerAndelsnummer,
        Map<RefusjonsandelDto, LocalDateTimeline<RefusjonsperiodeDto>> refusjonstidslinjerPerAndel, LocalDate periodeFom) {
        var andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.kopier(bgAndel);
        var matchendeRefusjonsandeler = refusjonsandelerPerAndelsnummer.getOrDefault(bgAndel.getAndelsnr(), List.of());
        if (!matchendeRefusjonsandeler.isEmpty()) {
            var refusjonsperiode = finnKrav(matchendeRefusjonsandeler, refusjonstidslinjerPerAndel, periodeFom);
            var arbeidsforholdBuilder = andelBuilder.getBgAndelArbeidsforholdDtoBuilder();
            arbeidsforholdBuilder.medRefusjon(lagRefusjon(refusjonsperiode, bgAndel.getBgAndelArbeidsforhold().orElseThrow().getRefusjon()));
            andelBuilder.medBGAndelArbeidsforhold(arbeidsforholdBuilder);
        }
        return andelBuilder;
    }

    private static void leggTilNyAndel(BeregningsgrunnlagInput input, BeregningsgrunnlagPeriodeDto.Builder periodeBuilder,
                                       RefusjonsandelDto refusjonsandel,
                                       Map<RefusjonsandelDto, LocalDateTimeline<RefusjonsperiodeDto>> refusjonstidslinjerPerAndel,
                                       LocalDate periodeFom) {
        var refusjonsperiode = finnKrav(List.of(refusjonsandel), refusjonstidslinjerPerAndel, periodeFom);
        if (refusjonsperiode == null || refusjonsperiode.refusjonPrMnd().compareTo(Beløp.ZERO) <= 0) {
            return;
        }
        var arbeidsperiode = finnArbeidsperiode(input, refusjonsandel);
        if (arbeidsperiode.filter(periode -> periode.getTom().isBefore(input.getBeregningsgrunnlag().getSkjæringstidspunkt())).isPresent()) {
            return;
        }
        var arbeidsforholdBuilder = BGAndelArbeidsforholdDto.builder().medArbeidsgiver(refusjonsandel.arbeidsgiver())
            .medArbeidsforholdRef(refusjonsandel.arbeidsforholdRef())
            .medRefusjon(lagRefusjon(refusjonsperiode, Optional.empty()));
        arbeidsperiode.ifPresent(periode -> arbeidsforholdBuilder.medArbeidsperiodeFom(periode.getFom()).medArbeidsperiodeTom(periode.getTom()));
        periodeBuilder.leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(arbeidsforholdBuilder).medKilde(AndelKilde.PROSESS_PERIODISERING)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medArbforholdType(OpptjeningAktivitetType.ARBEID));
    }

    private static Optional<Periode> finnArbeidsperiode(BeregningsgrunnlagInput input, RefusjonsandelDto refusjonsandel) {
        var skjæringstidspunkt = input.getSkjæringstidspunktForBeregning();
        var ansettelsesperioder = FinnYrkesaktiviteterForBeregningTjeneste.finnYrkesaktiviteter(
                input.getIayGrunnlag(), input.getBeregningsgrunnlagGrunnlag(), skjæringstidspunkt).stream()
            .filter(yrkesaktivitet -> yrkesaktivitet.gjelderFor(refusjonsandel.arbeidsgiver(), refusjonsandel.arbeidsforholdRef()))
            .flatMap(yrkesaktivitet -> yrkesaktivitet.getAlleAnsettelsesperioder().stream())
            .toList();
        return FinnAnsettelsesPeriode.finnMinMaksPeriode(ansettelsesperioder, skjæringstidspunkt);
    }

    private static Map<Long, List<RefusjonsandelDto>> finnMatchendeAndeler(BeregningsgrunnlagPeriodeDto opprinneligPeriode,
                                                                         Map<RefusjonsandelDto, ?> refusjonstidslinjerPerAndel) {
        var refusjonsandelerPerAndelsnummer = new HashMap<Long, List<RefusjonsandelDto>>();
        refusjonstidslinjerPerAndel.keySet().forEach(refusjonsandel -> {
            // Som den gamle inputmapperen: uspesifisert IM-ref treffer første andel, spesifisert ref krever likhet.
            opprinneligPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
                .filter(bgAndel -> bgAndel.getArbeidsgiver().filter(refusjonsandel.arbeidsgiver()::equals).isPresent())
                .filter(bgAndel -> !refusjonsandel.arbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()
                    || bgAndel.getArbeidsforholdRef().filter(refusjonsandel.arbeidsforholdRef()::equals).isPresent())
                .findFirst().ifPresent(bgAndel -> refusjonsandelerPerAndelsnummer
                    .computeIfAbsent(bgAndel.getAndelsnr(), andelsnummer -> new ArrayList<>()).add(refusjonsandel));
        });
        return refusjonsandelerPerAndelsnummer;
    }

    private static RefusjonsperiodeDto finnKrav(List<RefusjonsandelDto> refusjonsandeler,
                                                Map<RefusjonsandelDto, LocalDateTimeline<RefusjonsperiodeDto>> refusjonstidslinjerPerAndel,
                                                LocalDate dato) {
        var refusjonsperioder = refusjonsandeler.stream()
            .map(refusjonsandel -> refusjonstidslinjerPerAndel.get(refusjonsandel).getSegment(new LocalDateInterval(dato, dato)))
            .filter(refusjonssegment -> refusjonssegment != null)
            .map(LocalDateSegment::getValue).toList();
        if (refusjonsperioder.size() > 1) {
            throw new IllegalArgumentException("Overlappende refusjonsperioder treffer samme BG-andel.");
        }
        return refusjonsperioder.isEmpty() ? null : refusjonsperioder.getFirst();
    }

    private static Refusjon lagRefusjon(RefusjonsperiodeDto refusjonsperiode, Optional<Refusjon> eksisterendeRefusjon) {
        var refusjonskravPrÅr = refusjonsperiode == null ? Beløp.ZERO : refusjonsperiode.refusjonPrMnd().multipliser(12);
        var refusjonsutfall = refusjonsperiode == null ? Utfall.UDEFINERT : mapUtfall(refusjonsperiode);
        var skalNullstilleRefusjon = refusjonskravPrÅr.erNullEller0() || refusjonsutfall == Utfall.UNDERKJENT;
        return new Refusjon(refusjonskravPrÅr,
            eksisterendeRefusjon.map(Refusjon::getSaksbehandletRefusjonPrÅr)
                .map(beløp -> skalNullstilleRefusjon ? Beløp.ZERO : beløp).orElse(null),
            eksisterendeRefusjon.map(Refusjon::getFordeltRefusjonPrÅr)
                .map(beløp -> skalNullstilleRefusjon ? Beløp.ZERO : beløp).orElse(null),
            eksisterendeRefusjon.map(Refusjon::getManueltFordeltRefusjonPrÅr)
                .map(beløp -> skalNullstilleRefusjon ? Beløp.ZERO : beløp).orElse(null),
            eksisterendeRefusjon.map(Refusjon::getHjemmelForRefusjonskravfrist).orElse(null), refusjonsutfall);
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
