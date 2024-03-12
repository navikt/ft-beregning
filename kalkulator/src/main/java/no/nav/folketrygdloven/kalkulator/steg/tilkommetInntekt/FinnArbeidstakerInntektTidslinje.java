package no.nav.folketrygdloven.kalkulator.steg.tilkommetInntekt;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.inntektgradering.DagsatsPrKategoriOgArbeidsgiver;
import no.nav.folketrygdloven.kalkulator.felles.ytelseovergang.DirekteOvergangTjeneste;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektspostDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

// Lar denne ligge her enn så lenge i tilfelle det er noko vi skal ta i bruk som forbedring på tilkommet inntekt
class FinnArbeidstakerInntektTidslinje {


    /**
     * Lager tidslinje over periode der arbeidsfohold fra en arbeidsgiver anses å vere aktivt sett i kontekst av inntekt.
     *
     * @param skjæringstidspunkt           Skjæringstidspunkt
     * @param inntektposter                Inntektsposter fra a-ordningen
     * @param ytelser                      Ytelser
     * @param arbeidsgivere                Arbeidsgivere
     * @param utbetalingsgradGrunnlag      Grunnlag med utbetalingsgrader
     * @param inntektRapporteringsfristDag Dag for rapporteringsfrist pr måned
     * @return Tidslinje set av arbeidsgivere med godkjente inntekter
     */
    public static LocalDateTimeline<Set<Arbeidsgiver>> finnArbeidstakerInntektTidslinje(LocalDate skjæringstidspunkt,
                                                                                        Collection<InntektspostDto> inntektposter,
                                                                                        Collection<YtelseDto> ytelser,
                                                                                        List<Arbeidsgiver> arbeidsgivere,
                                                                                        UtbetalingsgradGrunnlag utbetalingsgradGrunnlag,
                                                                                        int inntektRapporteringsfristDag) {
        var inntektTidslinje = finnUtbetaltInntektTidslinje(skjæringstidspunkt, inntektposter, ytelser, arbeidsgivere);
        return UtvidetInntektsperiodeUtleder.lagGodkjenteInntektsperiodeTidslinje(inntektTidslinje, utbetalingsgradGrunnlag, inntektRapporteringsfristDag);
    }

    private static LocalDateTimeline<Set<DagsatsPrKategoriOgArbeidsgiver>> finnUtbetaltInntektTidslinje(LocalDate skjæringstidspunkt, Collection<InntektspostDto> inntektposter, Collection<YtelseDto> ytelser, List<Arbeidsgiver> arbeidsgivere) {
        var registerInntektTidslinje = RegisterInntektTidslinjeUtleder.lagInntektsperioderTidslinje(skjæringstidspunkt.minusMonths(3).withDayOfMonth(1), inntektposter, arbeidsgivere);
        var direkteUtbetalingTidslinje = finnTidslinjeFraDirekteMottattYtelse(ytelser);
        // TODO: Her kjører vi en godtroende union som ikke tar hensyn til at de to objektene kan være like, eks ved 50% ytelse der dagsats fra ytelse er lik dagsats fra arbeidsforhold
        return registerInntektTidslinje.combine(direkteUtbetalingTidslinje, StandardCombinators::union, LocalDateTimeline.JoinStyle.CROSS_JOIN);
    }

    private static LocalDateTimeline<Set<DagsatsPrKategoriOgArbeidsgiver>> finnTidslinjeFraDirekteMottattYtelse(Collection<YtelseDto> ytelser) {
        return DirekteOvergangTjeneste.direkteUtbetalingTidslinje(ytelser, y -> true)
                .mapValue(v -> v.stream().filter(a -> a.inntektskategori().equals(Inntektskategori.ARBEIDSTAKER)).collect(Collectors.toSet()))
                .filterValue(v -> !v.isEmpty());
    }

    public static LocalDateSegment<Set<TilkommetInntektsforholdTjeneste.Inntektsforhold>> kunGyldigePeriodeForInntekt(LocalDateInterval di, LocalDateSegment<Set<TilkommetInntektsforholdTjeneste.Inntektsforhold>> lhs,
                                                                                                                      LocalDateSegment<Set<Arbeidsgiver>> rhs) {
        if (lhs != null) {

            var aktiveInntektsforhold = lhs.getValue().stream()
                    .filter(it -> erIkkeArbeidstakerEllerHarInntektSomArbeidstaker(rhs, it))
                    .collect(Collectors.toSet());
            return new LocalDateSegment<>(di, aktiveInntektsforhold);
        }
        // Skal ikkje havne her ved LEFT_JOIN
        return null;
    }

    private static boolean erIkkeArbeidstakerEllerHarInntektSomArbeidstaker(LocalDateSegment<Set<Arbeidsgiver>> rhs, TilkommetInntektsforholdTjeneste.Inntektsforhold it) {
        return !it.aktivitetStatus().erArbeidstaker() || (rhs != null && rhs.getValue().stream().anyMatch(i -> i.equals(it.arbeidsgiver())));
    }

}
