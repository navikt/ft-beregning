package no.nav.folketrygdloven.kalkulator.felles.ytelseovergang;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

import no.nav.folketrygdloven.kalkulator.felles.inntektgradering.DagsatsPrKategoriOgArbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.iay.AnvistAndel;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public class DirekteOvergangTjeneste {

    private static final List<YtelseType> YTELSER_FRA_KAP_8 = List.of(
            YtelseType.PLEIEPENGER_NÆRSTÅENDE,
            YtelseType.PLEIEPENGER_SYKT_BARN,
            YtelseType.SYKEPENGER,
            YtelseType.FORELDREPENGER,
            YtelseType.OMSORGSPENGER,
            YtelseType.OPPLÆRINGSPENGER,
            YtelseType.SVANGERSKAPSPENGER
    );

    /**
     * Finner siste ytelseanvisning eller -anvisninger før skjæringstidspunktet for ytelser som beregnes fra folketrygdloven kapittel 8;
     * Sykepenger, Foreldrepenger, Svangerskapspenger, Pleiepenger, Omsorgspenger og Opplæringspenger
     *
     * @param iayGrunnlag        IAY-grunnlag
     * @param skjæringstidspunkt Skjæringstidspunkt for beregning
     * @return Liste med anvisninger
     */
    public static List<YtelseAnvistDto> finnAnvisningerForDirekteOvergangFraKap8(InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunkt) {
        return finnSisteAnvisninger(getYtelseFilterKap8(iayGrunnlag, skjæringstidspunkt), skjæringstidspunkt);
    }

    private static YtelseFilterDto getYtelseFilterKap8(InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunkt) {
        return new YtelseFilterDto(iayGrunnlag.getAktørYtelseFraRegister())
                .før(skjæringstidspunkt)
                .filter(y -> YTELSER_FRA_KAP_8.contains(y.getYtelseType()))
                .filter(y -> !y.getPeriode().getTomDato().isBefore(skjæringstidspunkt.minusMonths(3).withDayOfMonth(1)));
    }

    private static List<YtelseAnvistDto> finnSisteAnvisninger(YtelseFilterDto filter, LocalDate skjæringstidspunkt) {
        var ytelser = filter.getAlleYtelser();
        var alleAnvisninger = ytelser.stream()
                .flatMap(y -> y.getYtelseAnvist().stream()
                        .filter(ya -> ya.getAnvistPeriode().getFomDato().isBefore(skjæringstidspunkt)))
                .toList();
        var sisteDagMedAnvisning = alleAnvisninger.stream().max(Comparator.comparing(YtelseAnvistDto::getAnvistTOM)).map(YtelseAnvistDto::getAnvistTOM);
        return sisteDagMedAnvisning.isEmpty() ? Collections.emptyList() : alleAnvisninger.stream()
                .filter(a -> a.getAnvistTOM().equals(sisteDagMedAnvisning.get())).toList();
    }


    /**
     * Vurderer om det er mottatt ytelse direkte til bruker for arbeid hos gitt arbeidsgiver.
     * <p>
     * Dersom ytelsen ikke oppgir andeler antas det at ytelsen utbetales direkte.
     *
     * @param intervall    Periode som vurderes
     * @param arbeidsgiver Arbeidsgiver
     * @param ytelser      Ytelser
     * @return har direkte utbetalt ytelse for aktivitet hos arbeidsgiver
     */
    public static boolean harDirekteMottattYtelseForArbeidsgiver(Intervall intervall,
                                                                 Arbeidsgiver arbeidsgiver,
                                                                 Collection<YtelseDto> ytelser) {
        return ytelser.stream()
                .flatMap(y -> y.getYtelseAnvist().stream())
                .filter(ya -> ya.getAnvistPeriode().overlapper(intervall))
                .anyMatch(ya -> ya.getAnvisteAndeler().isEmpty() ||
                        ya.getAnvisteAndeler().stream()
                                .anyMatch(a -> erDirekteutbetalingForArbeidsgiver(arbeidsgiver, a) || erDirekteUtbetalingUtenArbeidsgiver(a)));
    }

    /**
     * Lager tidslinje over perioder med direkte mottatt ytelse for status/arbeidsgiver.
     * <p>
     * Dersom en ytelse ikke har en liste med andeler antas direkteutbetaling, men DagsatsPrStatusOgArbeidsgiver som returneres
     * har AktivitetStatus UDEFINERT og ingen arbeidsgiver.
     *
     * @param ytelser         Ytelser
     * @param ytelsePredicate ytelsepredicate for å filtrere bort ytelser
     * @return Tidslinje over perioder med direkte mottatt ytelse
     */
    public static LocalDateTimeline<Set<DagsatsPrKategoriOgArbeidsgiver>> direkteUtbetalingTidslinje(Collection<YtelseDto> ytelser, Predicate<? super YtelseDto> ytelsePredicate) {

        List<LocalDateSegment<Set<DagsatsPrKategoriOgArbeidsgiver>>> ytelserPrStatusOgArbeidsgiver = ytelser.stream()
                .filter(ytelsePredicate)
                .flatMap(y -> y.getYtelseAnvist().stream())
                .flatMap(y -> {
                    if (y.getAnvisteAndeler().isEmpty()) {
                        return Stream.of(new LocalDateSegment<>(y.getAnvistFOM(), y.getAnvistTOM(), Set.of(new DagsatsPrKategoriOgArbeidsgiver(Inntektskategori.UDEFINERT, null, null))));
                    }
                    return y.getAnvisteAndeler().stream().map(a -> mapTilDirekteUtbetalingSegment(a, y.getAnvistPeriode()));
                })
                .toList();
        return new LocalDateTimeline<>(ytelserPrStatusOgArbeidsgiver, StandardCombinators::union).filterValue(v -> !v.isEmpty()).mapValue(TreeSet::new);
    }


    private static LocalDateSegment<Set<DagsatsPrKategoriOgArbeidsgiver>> mapTilDirekteUtbetalingSegment(AnvistAndel a, Intervall anvistPeriode) {
        if (erDirekteutbetaling(a)) {
            return new LocalDateSegment<>(anvistPeriode.getFomDato(),
                    anvistPeriode.getTomDato(),
                    Set.of(new DagsatsPrKategoriOgArbeidsgiver(a.getInntektskategori(), a.getArbeidsgiver().orElse(null), finnDirekteUtbetaltDagsats(a))));
        }
        return new LocalDateSegment<>(anvistPeriode.getFomDato(),
                anvistPeriode.getTomDato(),
                Set.of());
    }

    private static Beløp finnDirekteUtbetaltDagsats(AnvistAndel a) {
        return a.getDagsats().multipliser(BigDecimal.valueOf(100).subtract(a.getRefusjonsgrad().verdi()).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
    }

    private static boolean erDirekteUtbetalingUtenArbeidsgiver(AnvistAndel a) {
        return a.getInntektskategori().equals(Inntektskategori.ARBEIDSTAKER) &&
                a.getArbeidsgiver().isEmpty() &&
                a.getRefusjonsgrad().compareTo(Stillingsprosent.HUNDRED) < 0;
    }

    private static boolean erDirekteutbetaling(AnvistAndel a) {
        return !a.getInntektskategori().equals(Inntektskategori.ARBEIDSTAKER) || a.getRefusjonsgrad().compareTo(Stillingsprosent.fra(100)) < 0;
    }

    private static boolean erDirekteutbetalingForArbeidsgiver(Arbeidsgiver arbeidsgiver, AnvistAndel a) {
        return a.getArbeidsgiver().isPresent() &&
                a.getArbeidsgiver().get().equals(arbeidsgiver) &&
                a.getRefusjonsgrad().compareTo(Stillingsprosent.HUNDRED) < 0;
    }


}
