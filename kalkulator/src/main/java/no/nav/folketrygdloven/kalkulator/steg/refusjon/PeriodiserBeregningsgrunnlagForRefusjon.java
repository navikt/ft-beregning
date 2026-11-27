package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsandel;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.Refusjonsperiode;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Refusjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.RefusjonUtfall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public final class PeriodiserBeregningsgrunnlagForRefusjon {

    private PeriodiserBeregningsgrunnlagForRefusjon() {
    }

    public static BeregningsgrunnlagDto periodiser(BeregningsgrunnlagInput input, FordelingGrunnlag fordelingGrunnlag) {
        var eksisterendeBeregningsgrunnlag = Objects.requireNonNull(input.getBeregningsgrunnlag(),
            "Beregningsgrunnlag må være satt.");
        var skjæringstidspunkt = eksisterendeBeregningsgrunnlag.getSkjæringstidspunkt();
        var gjeldendeRefusjonAndeler = fordelingGrunnlag.hentGjeldendeAndeler();
        var periodeÅrsakerPerDato = finnPeriodeÅrsaker(gjeldendeRefusjonAndeler);
        var grunnlagBuilder = BeregningsgrunnlagDto.builder(eksisterendeBeregningsgrunnlag).fjernAllePerioder();

        eksisterendeBeregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(periode -> periodiserPeriode(input, periode, gjeldendeRefusjonAndeler, periodeÅrsakerPerDato,
                skjæringstidspunkt).stream())
            .forEach(grunnlagBuilder::leggTilBeregningsgrunnlagPeriode);

        return grunnlagBuilder.build();
    }

    private static LocalDateTimeline<Refusjonsperiode> lagTidslinje(Refusjonsandel refusjonsandel) {
        var refusjonssegmenter = refusjonsandel.refusjonsperioder().stream().map(refusjonsperiode -> {
            mapUtfall(refusjonsperiode);
            if (refusjonsperiode.refusjonPrMnd() == null || refusjonsperiode.refusjonPrMnd().compareTo(Beløp.ZERO) < 0) {
                throw new IllegalArgumentException("Refusjonsperioden må ha et ikke-negativt månedsbeløp.");
            }
            return new LocalDateSegment<>(refusjonsperiode.periode().getFomDato(), refusjonsperiode.periode().getTomDato(), refusjonsperiode);
        }).toList();
        return new LocalDateTimeline<>(refusjonssegmenter);
    }

    private static Map<LocalDate, List<PeriodeÅrsak>> finnPeriodeÅrsaker(List<Refusjonsandel> gjeldendeAndeler) {
        var periodeÅrsakerPerDato = new TreeMap<LocalDate, List<PeriodeÅrsak>>();
        gjeldendeAndeler.stream()
            .map(PeriodiserBeregningsgrunnlagForRefusjon::lagTidslinje)
            .forEach(refusjonstidslinje -> leggTilPeriodeÅrsaker(refusjonstidslinje, periodeÅrsakerPerDato));
        return periodeÅrsakerPerDato;
    }

    private static void leggTilPeriodeÅrsaker(LocalDateTimeline<Refusjonsperiode> refusjonstidslinje,
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

    private static List<BeregningsgrunnlagPeriodeDto.Builder> periodiserPeriode(
        BeregningsgrunnlagInput input,
        BeregningsgrunnlagPeriodeDto opprinneligPeriode,
        List<Refusjonsandel> gjeldendeAndeler,
        Map<LocalDate, List<PeriodeÅrsak>> periodeÅrsakerPerDato,
        LocalDate skjæringstidspunkt) {
        var refusjonsandelerPerAndelsnummer = finnMatchendeAndeler(opprinneligPeriode, gjeldendeAndeler);
        var periodestartdatoer = new TreeSet<LocalDate>();
        periodestartdatoer.add(opprinneligPeriode.getBeregningsgrunnlagPeriodeFom());
        periodeÅrsakerPerDato.keySet().stream().filter(opprinneligPeriode.getPeriode()::inkluderer).forEach(periodestartdatoer::add);
        return periodestartdatoer.stream().map(periodeFom -> {
            var nestePeriodestartdato = periodestartdatoer.higher(periodeFom);
            var periodeTom = nestePeriodestartdato == null
                ? opprinneligPeriode.getBeregningsgrunnlagPeriodeTom() : nestePeriodestartdato.minusDays(1);
            return lagPeriode(input, opprinneligPeriode, periodeFom, periodeTom,
                refusjonsandelerPerAndelsnummer, gjeldendeAndeler, periodeÅrsakerPerDato, skjæringstidspunkt);
        }).toList();
    }

    private static BeregningsgrunnlagPeriodeDto.Builder lagPeriode(
        BeregningsgrunnlagInput input, BeregningsgrunnlagPeriodeDto opprinneligPeriode, LocalDate periodeFom, LocalDate periodeTom,
        Map<Long, List<Refusjonsandel>> refusjonsandelerPerAndelsnummer,
        List<Refusjonsandel> gjeldendeAndeler,
        Map<LocalDate, List<PeriodeÅrsak>> periodeÅrsakerPerDato,
        LocalDate skjæringstidspunkt) {
        var periodeBuilder = BeregningsgrunnlagPeriodeDto.kopier(opprinneligPeriode).fjernAlleBeregningsgrunnlagPrStatusOgAndeler()
            .medBeregningsgrunnlagPeriode(periodeFom, periodeTom);
        if (!periodeFom.equals(skjæringstidspunkt)) {
            periodeBuilder.leggTilPeriodeÅrsaker(periodeÅrsakerPerDato.getOrDefault(periodeFom, List.of()));
        }
        //eksisterende bgandel kopieres med oppdatert refusjon, hvis ingen endring blir refusjonen uendret
        opprinneligPeriode.getBeregningsgrunnlagPrStatusOgAndelList().stream()
            .map(bgAndel -> kopierAndelMedRefusjon(bgAndel, refusjonsandelerPerAndelsnummer, periodeFom))
            .forEach(periodeBuilder::leggTilBeregningsgrunnlagPrStatusOgAndel);
        //Hvis refusjonsandel ikke finnes fra før, opprettes ny bgAndel med ny refusjon
        gjeldendeAndeler.stream()
            .filter(refusjonsandel -> refusjonsandelerPerAndelsnummer.values().stream()
                .noneMatch(refusjonPerAndelsnummer -> refusjonPerAndelsnummer.contains(refusjonsandel)))
            .forEach(refusjonsandel -> leggTilNyAndel(input, periodeBuilder, refusjonsandel, periodeFom, skjæringstidspunkt));

        // Andelsbuilderen summerer brutto på nytt. Periodens øvrige beregningsdata skal beholdes.
        return periodeBuilder.medBruttoPrÅr(opprinneligPeriode.getBruttoPrÅr());
    }

    private static BeregningsgrunnlagPrStatusOgAndelDto.Builder kopierAndelMedRefusjon(
        BeregningsgrunnlagPrStatusOgAndelDto bgAndel, Map<Long, List<Refusjonsandel>> refusjonsandelerPerAndelsnummer,
        LocalDate periodeFom) {
        var andelBuilder = BeregningsgrunnlagPrStatusOgAndelDto.kopier(bgAndel);
        var matchendeRefusjonsandeler = refusjonsandelerPerAndelsnummer.getOrDefault(bgAndel.getAndelsnr(), List.of());
        if (!matchendeRefusjonsandeler.isEmpty()) {
            var refusjonskrav = finnRefusjonskrav(matchendeRefusjonsandeler, periodeFom);
            var arbeidsforholdBuilder = andelBuilder.getBgAndelArbeidsforholdDtoBuilder();
            arbeidsforholdBuilder.medRefusjon(lagRefusjon(refusjonskrav, bgAndel.getBgAndelArbeidsforhold().orElseThrow().getRefusjon()));
            andelBuilder.medBGAndelArbeidsforhold(arbeidsforholdBuilder);
        }
        return andelBuilder;
    }

    private static void leggTilNyAndel(BeregningsgrunnlagInput input, BeregningsgrunnlagPeriodeDto.Builder periodeBuilder,
                                       Refusjonsandel refusjonsandel, LocalDate periodeFom, LocalDate skjæringstidspunkt) {
        var refusjonskrav = finnRefusjonskrav(List.of(refusjonsandel), periodeFom);
        if (refusjonskrav.periode() == null || refusjonskrav.periode().refusjonPrMnd().compareTo(Beløp.ZERO) <= 0) {
            return;
        }
        var arbeidsperiode = finnArbeidsperiode(input, refusjonsandel);
        if (arbeidsperiode.filter(periode -> periode.getTom().isBefore(skjæringstidspunkt)).isPresent()) {
            return;
        }
        var arbeidsforholdBuilder = BGAndelArbeidsforholdDto.builder().medArbeidsgiver(refusjonsandel.arbeidsgiver())
            .medArbeidsforholdRef(refusjonsandel.arbeidsforholdRef())
            .medRefusjon(lagRefusjon(refusjonskrav, Optional.empty()));
        arbeidsperiode.ifPresent(periode -> arbeidsforholdBuilder.medArbeidsperiodeFom(periode.getFom()).medArbeidsperiodeTom(periode.getTom()));
        periodeBuilder.leggTilBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPrStatusOgAndelDto.ny()
            .medBGAndelArbeidsforhold(arbeidsforholdBuilder).medKilde(AndelKilde.PROSESS_PERIODISERING)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).medArbforholdType(OpptjeningAktivitetType.ARBEID));
    }

    private static Optional<Periode> finnArbeidsperiode(BeregningsgrunnlagInput input, Refusjonsandel refusjonsandel) {
        var skjæringstidspunkt = input.getSkjæringstidspunktForBeregning();
        var ansettelsesperioder = FinnYrkesaktiviteterForBeregningTjeneste.finnYrkesaktiviteter(
                input.getIayGrunnlag(), input.getBeregningsgrunnlagGrunnlag(), skjæringstidspunkt).stream()
            .filter(yrkesaktivitet -> yrkesaktivitet.gjelderFor(refusjonsandel.arbeidsgiver(), refusjonsandel.arbeidsforholdRef()))
            .flatMap(yrkesaktivitet -> yrkesaktivitet.getAlleAnsettelsesperioder().stream())
            .toList();
        return FinnAnsettelsesPeriode.finnMinMaksPeriode(ansettelsesperioder, skjæringstidspunkt);
    }

    private static Map<Long, List<Refusjonsandel>> finnMatchendeAndeler(BeregningsgrunnlagPeriodeDto opprinneligPeriode,
                                                                        List<Refusjonsandel> gjeldendeAndeler) {
        var refusjonsandelerPerAndelsnummer = new HashMap<Long, List<Refusjonsandel>>();
        gjeldendeAndeler.forEach(refusjonsandel -> {
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

    private static Refusjonskrav finnRefusjonskrav(List<Refusjonsandel> refusjonsandeler, LocalDate dato) {
        return refusjonsandeler.stream()
            .flatMap(andel -> andel.refusjonsperioder().stream()
                .filter(periode -> periode.periode().inkluderer(dato))
                .map(periode -> new Refusjonskrav(periode, andel.kilde())))
            .reduce((_, _) -> {
                throw new IllegalArgumentException("Overlappende refusjonsperioder treffer samme BG-andel.");
            })
            .orElseGet(() -> new Refusjonskrav(null, finnRefusjonKilde(refusjonsandeler)));
    }

    private static RefusjonKilde finnRefusjonKilde(List<Refusjonsandel> refusjonsandeler) {
        return refusjonsandeler.stream()
            .map(Refusjonsandel::kilde)
            .filter(RefusjonKilde.SAKSBEHANDLER::equals)
            .findFirst()
            .orElse(RefusjonKilde.INNTEKTSMELDING);
    }

    private record Refusjonskrav(Refusjonsperiode periode, RefusjonKilde kilde) {
        Beløp beløpPrÅr() {
            return periode == null ? Beløp.ZERO : periode.refusjonPrMnd().multipliser(12);
        }

        Utfall utfall() {
            return periode == null ? Utfall.UDEFINERT : mapUtfall(periode);
        }

        boolean skalNullstille() {
            return beløpPrÅr().erNullEller0() || utfall() == Utfall.UNDERKJENT;
        }
    }

    private static Refusjon lagRefusjon(Refusjonskrav nyttRefusjonskrav, Optional<Refusjon> eksisterendeRefusjonBgAndel) {
        var nyttRefusjonsbeløpPrÅr = nyttRefusjonskrav.beløpPrÅr();
        var skalNullstilles = nyttRefusjonskrav.skalNullstille();
        var fordeltRefusjonBeløp = videreførEllerNullstill(eksisterendeRefusjonBgAndel.map(Refusjon::getFordeltRefusjonPrÅr), skalNullstilles);
        var manueltFordeltRefusjon = videreførEllerNullstill(eksisterendeRefusjonBgAndel.map(Refusjon::getManueltFordeltRefusjonPrÅr), skalNullstilles);
        var hjemmel = eksisterendeRefusjonBgAndel.map(Refusjon::getHjemmelForRefusjonskravfrist).orElse(null);

        return switch (nyttRefusjonskrav.kilde()) {
            case INNTEKTSMELDING -> new Refusjon(
                nyttRefusjonsbeløpPrÅr,
                videreførEllerNullstill(eksisterendeRefusjonBgAndel.map(Refusjon::getSaksbehandletRefusjonPrÅr), skalNullstilles),
                fordeltRefusjonBeløp, manueltFordeltRefusjon, hjemmel, nyttRefusjonskrav.utfall());
            case SAKSBEHANDLER -> new Refusjon(
                eksisterendeRefusjonBgAndel.map(Refusjon::getRefusjonskravPrÅr).orElse(null),
                skalNullstilles ? Beløp.ZERO : nyttRefusjonsbeløpPrÅr,
                fordeltRefusjonBeløp, manueltFordeltRefusjon, hjemmel, nyttRefusjonskrav.utfall());
        };
    }

    private static Beløp videreførEllerNullstill(Optional<Beløp> beløp, boolean skalNullstille) {
        return beløp.map(b -> skalNullstille ? Beløp.ZERO : b).orElse(null);
    }

    private static Utfall mapUtfall(Refusjonsperiode periode) {
        if (periode.utfall() == null) {
            throw new IllegalArgumentException("Refusjonsperioden mangler utfall.");
        }
        return switch (periode.utfall()) {
            case INNVILGET, REDUSERT -> Utfall.GODKJENT;
            case AVSLÅTT -> Utfall.UNDERKJENT;
        };
    }

    private static PeriodeÅrsak utledÅrsak(Refusjonsperiode periode) {
        if (periode.utfall() == RefusjonUtfall.AVSLÅTT) {
            return PeriodeÅrsak.REFUSJON_AVSLÅTT;
        }
        return periode.refusjonPrMnd().erNullEller0() ? PeriodeÅrsak.REFUSJON_OPPHØRER : PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV;
    }
}
