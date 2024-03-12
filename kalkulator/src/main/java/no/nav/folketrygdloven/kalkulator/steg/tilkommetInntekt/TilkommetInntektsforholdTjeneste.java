package no.nav.folketrygdloven.kalkulator.steg.tilkommetInntekt;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.UtbetalingsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.input.YtelsespesifiktGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndelDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørArbeidDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonPerYrkesaktivitet;
import no.nav.folketrygdloven.kalkulator.modell.svp.PeriodeMedUtbetalingsgradDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Aktivitetsgrad;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.StatusOgArbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.AndelKilde;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

/**
 * Ved nye inntektsforhold skal beregningsgrunnlaget graderes mot inntekt.
 * <p>
 * Utleder her om det er potensielle nye inntektsforhold.
 * <p>
 * Se <a href="https://navno.sharepoint.com/sites/fag-og-ytelser-arbeid-sykefravarsoppfolging-og-sykepenger/SitePages/%C2%A7-8-13-Graderte-sykepenger.aspx">...</a>
 */
public class TilkommetInntektsforholdTjeneste {

    /**
     * Utleder tidslinje over tilkommet inntektsforhold
     * <p>
     * Bestemmer hvilke statuser/arbeidsgivere som skal regnes som nytt
     * <p>
     * Dersom en inntekt/aktivitet regnes som nytt skal beregningsgrunnlaget graderes mot inntekt i denne perioden. Dette betyr at inntekt i tillegg til ytelsen kan føre til nedjustering av utbetalt ytelse.
     * <p>
     * Et inntektsforhold regnes som nytt dersom:
     * - Den fører til at bruker har en ekstra inntekt i tillegg til det hen ville ha hatt om hen ikke mottok ytelse
     * - Inntekten ikke erstatter inntekt i et arbeidsforhold som er avsluttet
     * - Det ikke er fullt fravær i arbeidsforholdet/aktiviteten (har opprettholdt noe arbeid og dermed sannsynligvis inntekt)
     * <p>
     * Vi antar bruker ville opprettholdt arbeid hos arbeidsgivere der bruker fortsatt er innregistrert i aareg, og at dette regner som en løpende aktivitet.
     * Dersom antall løpende aktiviteter øker, skal saksbehandler vurdere om de tilkomne aktivitetene skal føre til reduksjon i utbetaling.
     *
     * @param skjæringstidspunkt      Skjæringstidspunkt for beregning
     * @param andelerFraStart         Andeler i første periode
     * @param utbetalingsgradGrunnlag Ytelsesspesifikt grunnlag
     * @param iayGrunnlag             Iay-grunnlag
     * @return Tidslinje for tilkommet aktivitet/inntektsforhold
     */
    public static LocalDateTimeline<Set<StatusOgArbeidsgiver>> finnTilkommetInntektsforholdTidslinje(LocalDate skjæringstidspunkt,
                                                                                                     Collection<BeregningsgrunnlagPrStatusOgAndelDto> andelerFraStart,
                                                                                                     YtelsespesifiktGrunnlag utbetalingsgradGrunnlag,
                                                                                                     InntektArbeidYtelseGrunnlagDto iayGrunnlag) {
        return finnTilkommetInntektsforholdTidslinje(skjæringstidspunkt, andelerFraStart, utbetalingsgradGrunnlag, iayGrunnlag, false);
    }

    public static LocalDateTimeline<Set<StatusOgArbeidsgiver>> finnTilkommetInntektsforholdTidslinje(LocalDate skjæringstidspunkt,
                                                                                                     Collection<BeregningsgrunnlagPrStatusOgAndelDto> andelerFraStart,
                                                                                                     YtelsespesifiktGrunnlag utbetalingsgradGrunnlag,
                                                                                                     InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                                                     boolean ikkeFiltrerVedFulltFravær) {

        var yrkesaktiviteter = iayGrunnlag.getAktørArbeidFraRegister().map(AktørArbeidDto::hentAlleYrkesaktiviteter).orElse(Collections.emptyList());
        var yrkesaktivitetTidslinje = finnInntektsforholdFraYrkesaktiviteter(skjæringstidspunkt, yrkesaktiviteter);
        var næringTidslinje = finnInntektsforholdForStatusFraFravær((UtbetalingsgradGrunnlag) utbetalingsgradGrunnlag, AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        var frilansTidslinje = finnInntektsforholdForStatusFraFravær((UtbetalingsgradGrunnlag) utbetalingsgradGrunnlag, AktivitetStatus.FRILANSER);
        var aktivitetTidslinje = yrkesaktivitetTidslinje.union(næringTidslinje, StandardCombinators::union)
                .union(frilansTidslinje, StandardCombinators::union);

        if (!ikkeFiltrerVedFulltFravær) {
            var utbetalingTidslinje = finnTidslinjeMedFravær((UtbetalingsgradGrunnlag) utbetalingsgradGrunnlag);
            aktivitetTidslinje = aktivitetTidslinje.intersection(utbetalingTidslinje, StandardCombinators::leftOnly);
        }
        return aktivitetTidslinje.compress().map(s -> mapTilkommetTidslinje(andelerFraStart, yrkesaktiviteter, utbetalingsgradGrunnlag, s, ikkeFiltrerVedFulltFravær));
    }

    private static LocalDateTimeline<Boolean> finnTidslinjeMedFravær(UtbetalingsgradGrunnlag utbetalingsgradGrunnlag) {
        return utbetalingsgradGrunnlag.getUtbetalingsgradPrAktivitet().stream()
                .flatMap(a -> a.getPeriodeMedUtbetalingsgrad().stream())
                .filter(p -> p.getAktivitetsgrad().map(v -> v.compareTo(Aktivitetsgrad.HUNDRE) < 0).orElse(false))
                .map(p -> new LocalDateSegment<>(p.getPeriode().getFomDato(), p.getPeriode().getTomDato(), Boolean.TRUE))
                .collect(Collectors.collectingAndThen(Collectors.toList(), s -> new LocalDateTimeline<>(s, StandardCombinators::alwaysTrueForMatch)));
    }

    private static List<LocalDateSegment<Set<StatusOgArbeidsgiver>>> mapTilkommetTidslinje(Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler,
                                                                                           Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                           YtelsespesifiktGrunnlag utbetalingsgradGrunnlag,
                                                                                           LocalDateSegment<Set<Inntektsforhold>> s,
                                                                                           boolean ikkeFiltrerVedFulltFravær) {
        var periode = Intervall.fraOgMedTilOgMed(s.getFom(), s.getTom());
        return List.of(new LocalDateSegment<>(s.getFom(), s.getTom(),
                mapTilkomne(yrkesaktiviteter, andeler, utbetalingsgradGrunnlag, periode, s.getValue(), ikkeFiltrerVedFulltFravær)));
    }

    private static Set<StatusOgArbeidsgiver> mapTilkomne(Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                         Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler,
                                                         YtelsespesifiktGrunnlag utbetalingsgradGrunnlag,
                                                         Intervall periode,
                                                         Set<Inntektsforhold> inntektsforholdListe,
                                                         boolean ikkeFiltrerVedFulltFravær) {


        var nyeInntektsforhold = inntektsforholdListe.stream()
                .filter(it -> !harAndelForArbeidsgiverFraStart(it, andeler))
                .filter(it -> ikkeFiltrerVedFulltFravær || harIkkeFulltFravær(utbetalingsgradGrunnlag, periode, it))
                .collect(Collectors.toSet());

        return nyeInntektsforhold.stream()
                .sorted(sorterPåStartdato(yrkesaktiviteter))
                .map(it -> new StatusOgArbeidsgiver(it.aktivitetStatus(), it.arbeidsgiver()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

    }

    private static boolean harIkkeFulltFravær(YtelsespesifiktGrunnlag utbetalingsgradGrunnlag, Intervall periode, Inntektsforhold it) {
        var aktivitetsgrad = finnAktivitetsgrad(periode, utbetalingsgradGrunnlag, it);
        return harIkkeFulltFravær(aktivitetsgrad);
    }

    private static LocalDateTimeline<Set<Inntektsforhold>> finnInntektsforholdFraYrkesaktiviteter(LocalDate skjæringstidspunkt, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        return  yrkesaktiviteter
                .stream()
                .filter(ya -> !mapTilAktivitetStatus(ya).equals(AktivitetStatus.UDEFINERT))
                .flatMap(ya -> {
                            var ansettelsesTidslinje = finnAnsettelseTidslinje(ya);
                            var permisjonTidslinje = PermisjonPerYrkesaktivitet.utledPermisjonPerYrkesaktivitet(ya, Collections.emptyMap(), skjæringstidspunkt);
                            return ansettelsesTidslinje.disjoint(permisjonTidslinje)
                                    .toSegments().stream()
                                    .map(LocalDateSegment::getLocalDateInterval)
                                    .filter(p -> p.getTomDato().isAfter(BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunkt)))
                                    .map(p -> new LocalDateSegment<>(
                                            p.getFomDato(),
                                            p.getTomDato(),
                                            Set.of(mapTilInntektsforhold(ya))));
                        }
                )
                .collect(Collectors.collectingAndThen(Collectors.toList(), s -> new LocalDateTimeline<>(s, StandardCombinators::union)));

    }

    private static Inntektsforhold mapTilInntektsforhold(YrkesaktivitetDto ya) {
        var aktivitetStatus = mapTilAktivitetStatus(ya);
        if (aktivitetStatus.equals(AktivitetStatus.ARBEIDSTAKER)) {
            return new Inntektsforhold(aktivitetStatus, ya.getArbeidsgiver(), ya.getArbeidsforholdRef());
        } else {
            return new Inntektsforhold(aktivitetStatus, null, null);
        }
    }

    private static LocalDateTimeline<Set<Inntektsforhold>> finnInntektsforholdForStatusFraFravær(UtbetalingsgradGrunnlag utbetalinger, AktivitetStatus status) {
        var perioderMedStatus = finnPerioderMedStatus(utbetalinger, mapTilUttakArbeidType(status));
        return perioderMedStatus.stream()
                .filter(TilkommetInntektsforholdTjeneste::harIkkeFulltFravær)
                .map(p -> new LocalDateSegment<>(p.getPeriode().getFomDato(), p.getPeriode().getTomDato(), Set.of(new Inntektsforhold(status, null, null))))
                .collect(Collectors.collectingAndThen(Collectors.toList(), s -> new LocalDateTimeline<>(s, StandardCombinators::union)));
    }

    private static Boolean harIkkeFulltFravær(PeriodeMedUtbetalingsgradDto p) {
        return p.getAktivitetsgrad().map(v -> v.compareTo(Aktivitetsgrad.ZERO) > 0).orElse(false);
    }

    private static UttakArbeidType mapTilUttakArbeidType(AktivitetStatus status) {
        return switch (status) {
            case SELVSTENDIG_NÆRINGSDRIVENDE -> UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE;
            case FRILANSER -> UttakArbeidType.FRILANS;
            default ->
                    throw new IllegalStateException("Støtter ikke tilkommet inntektsforhold fra fravær for status " + status);
        };
    }

    private static List<PeriodeMedUtbetalingsgradDto> finnPerioderMedStatus(UtbetalingsgradGrunnlag utbetalinger, UttakArbeidType uttakArbeidType) {
        return utbetalinger.getUtbetalingsgradPrAktivitet().stream()
                .filter(a -> a.getUtbetalingsgradArbeidsforhold().getUttakArbeidType().equals(uttakArbeidType))
                .flatMap(a -> a.getPeriodeMedUtbetalingsgrad().stream())
                .toList();
    }

    private static Optional<Aktivitetsgrad> finnAktivitetsgrad(Intervall periode,
                                                               YtelsespesifiktGrunnlag utbetalingsgradGrunnlag,
                                                               Inntektsforhold inntektsforhold) {
        if (inntektsforhold.aktivitetStatus().erArbeidstaker()) {
            return UtbetalingsgradTjeneste.finnAktivitetsgradForArbeid(
                    inntektsforhold.arbeidsgiver(),
                    inntektsforhold.arbeidsforholdRef(),
                    periode, utbetalingsgradGrunnlag, true);
        } else {
            return UtbetalingsgradTjeneste.finnAktivitetsgradForStatus(
                    inntektsforhold.aktivitetStatus(),
                    periode,
                    utbetalingsgradGrunnlag);
        }
    }

    private static AktivitetStatus mapTilAktivitetStatus(YrkesaktivitetDto yrkesaktivitet) {
        return switch (yrkesaktivitet.getArbeidType()) {
            case FORENKLET_OPPGJØRSORDNING, MARITIMT_ARBEIDSFORHOLD, ORDINÆRT_ARBEIDSFORHOLD ->
                    AktivitetStatus.ARBEIDSTAKER;
            case FRILANSER_OPPDRAGSTAKER -> AktivitetStatus.FRILANSER;
            case NÆRING -> AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE;
            default -> AktivitetStatus.UDEFINERT;
        };
    }

    private static boolean harIkkeFulltFravær(Optional<Aktivitetsgrad> aktivitetsgrad) {
        return aktivitetsgrad.map(it -> it.compareTo(Aktivitetsgrad.ZERO) > 0).orElse(true);
    }

    private static LocalDateTimeline<Boolean> finnAnsettelseTidslinje(YrkesaktivitetDto it) {
        return new LocalDateTimeline<>(it.getAlleAnsettelsesperioder()
                .stream()
                .map(p -> new LocalDateSegment<>(p.getPeriode().getFomDato(), p.getPeriode().getTomDato(), Boolean.TRUE))
                .toList(), StandardCombinators::alwaysTrueForMatch);
    }

    private static Comparator<Inntektsforhold> sorterPåStartdato(Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        return (i1, i2) -> {
            var ansattperioder1 = finnAnsattperioder(yrkesaktiviteter, i1);
            var ansattperioder2 = finnAnsattperioder(yrkesaktiviteter, i2);
            if (ansattperioder1.isEmpty()) {
                return ansattperioder2.isEmpty() ? 0 : 1;
            }
            if (ansattperioder2.isEmpty()) {
                return -1;
            }
            var førsteAnsattdato1 = finnFørsteAnsattdato(ansattperioder1);
            var førsteAnsattdato2 = finnFørsteAnsattdato(ansattperioder2);
            return førsteAnsattdato1.compareTo(førsteAnsattdato2);
        };
    }

    private static List<AktivitetsAvtaleDto> finnAnsattperioder(Collection<YrkesaktivitetDto> yrkesaktiviteter, Inntektsforhold a1) {
        return yrkesaktiviteter.stream().filter(ya -> ya.getArbeidsgiver().equals(a1.arbeidsgiver()) && ya.getArbeidsforholdRef().gjelderFor(a1.arbeidsforholdRef()))
                .flatMap(ya -> ya.getAlleAnsettelsesperioder().stream())
                .toList();
    }

    private static boolean harAndelForArbeidsgiverFraStart(Inntektsforhold inntektsforhold, Collection<BeregningsgrunnlagPrStatusOgAndelDto> andeler) {
        var matchendeAndeler1 = andeler.stream().filter(a -> Objects.equals(a.getArbeidsgiver().orElse(null), inntektsforhold.arbeidsgiver()) && a.getAktivitetStatus().equals(inntektsforhold.aktivitetStatus()))
                .toList();
        return matchendeAndeler1.stream().map(BeregningsgrunnlagPrStatusOgAndelDto::getKilde)
                .anyMatch(AndelKilde.PROSESS_START::equals);
    }

    private static LocalDate finnFørsteAnsattdato(Collection<AktivitetsAvtaleDto> ansattperioder1) {
        return ansattperioder1.stream().map(AktivitetsAvtaleDto::getPeriode)
                .map(Intervall::getFomDato)
                .min(Comparator.naturalOrder()).orElseThrow();
    }

    record Inntektsforhold(AktivitetStatus aktivitetStatus, Arbeidsgiver arbeidsgiver,
                           InternArbeidsforholdRefDto arbeidsforholdRef) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Inntektsforhold that = (Inntektsforhold) o;
            return aktivitetStatus == that.aktivitetStatus && Objects.equals(arbeidsgiver, that.arbeidsgiver) && Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef);
        }

        @Override
        public int hashCode() {
            return Objects.hash(aktivitetStatus, arbeidsgiver, arbeidsforholdRef);
        }

        @Override
        public InternArbeidsforholdRefDto arbeidsforholdRef() {
            return arbeidsforholdRef == null ? InternArbeidsforholdRefDto.nullRef() : arbeidsforholdRef;
        }
    }


}
