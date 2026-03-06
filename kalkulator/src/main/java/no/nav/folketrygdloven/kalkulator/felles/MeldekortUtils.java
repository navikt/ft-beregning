package no.nav.folketrygdloven.kalkulator.felles;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Stillingsprosent;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

public class MeldekortUtils {

    // Gammel konvensjon fra Arena der full utbetaling i 2 uker er 200%
    public static final BigDecimal ARENA_DIVISOR = BigDecimal.valueOf(2);

    public record UtbetalingMedNormertUtbetalingsprosent(YtelseAnvistDto utbetaling, boolean fullUtbetaling, Stillingsprosent normertUtbetalingsprosent) {}

    private MeldekortUtils() {}

    public static Optional<YtelseDto> sisteVedtakFørStpForType(YtelseFilterDto ytelseFilter, LocalDate skjæringstidspunkt, Set<YtelseType> ytelseTyper) {
        return ytelseFilter.getFiltrertYtelser().stream()
            .filter(ytelse -> ytelseTyper.contains(ytelse.getYtelseType()))
            .filter(ytelse -> !skjæringstidspunkt.isBefore(ytelse.getPeriode().getFomDato()))
            .max(Comparator.comparing(YtelseDto::getPeriode).thenComparing(ytelse -> ytelse.getPeriode().getTomDato()));
    }

    public static Optional<UtbetalingMedNormertUtbetalingsprosent> sisteHeleMeldekortFørStp(YtelseFilterDto ytelseFilter, YtelseDto sisteVedtak,
                                                                                            LocalDate skjæringstidspunkt, Set<YtelseType> ytelseTyper) {
        // Denne kan være fra 1 dag til N måneder før skjæringstidspunkt. Se heller på STP-10M elns?
        final var sisteVedtakFom = sisteVedtak.getPeriode().getFomDato();

        var alleMeldekort = finnAlleMeldekort(ytelseFilter, ytelseTyper);

        var sisteMeldekortOpt = alleMeldekort.stream()
            .filter(u -> sisteVedtakFom.minus(KonfigTjeneste.getMeldekortPeriode()).isBefore(u.utbetaling().getAnvistTOM()))
            .filter(u -> skjæringstidspunkt.isAfter(u.utbetaling().getAnvistTOM()))
            .max(Comparator.comparing(u -> u.utbetaling().getAnvistFOM()));

        if (sisteMeldekortOpt.isEmpty()) {
            return Optional.empty();
        }

        var sisteMeldekort = sisteMeldekortOpt.get();

        // Vi er nødt til å sjekke om vi har flere meldekort med samme periode
        // TODO: Er dette virkelig nødvendig? Har vi noen tilfelle i praksis? Det lar seg sjekke.
        var alleMeldekortMedPeriode = alleMeldekortMedPeriode(sisteMeldekort, alleMeldekort);
        if (alleMeldekortMedPeriode.size() > 1) {
            return finnMeldekortSomGjelderForVedtak(alleMeldekortMedPeriode, sisteVedtak);
        }

        return Optional.of(sisteMeldekort);

    }

    public static boolean finnesMeldekortSomInkludererGittDato(YtelseFilterDto ytelseFilter, YtelseDto sisteVedtak, Set<YtelseType> ytelseTyper, LocalDate gittDato) {
        var alleMeldekort = finnAlleMeldekort(ytelseFilter, ytelseTyper);

        var sisteMeldekort = alleMeldekort.stream()
            .filter(ytelseAnvist -> !gittDato.isBefore(ytelseAnvist.utbetaling().getAnvistFOM()))
            .filter(ytelseAnvist -> !gittDato.isAfter(ytelseAnvist.utbetaling().getAnvistTOM()))
            .max(Comparator.comparing(u -> u.utbetaling().getAnvistFOM()));

        if (sisteMeldekort.isEmpty()) {
            return false;
        }

        // Er dette virkelig nødvendig? Har vi noen tilfelle i praksis? Det lar seg sjekke.
        var alleMeldekortMedPeriode = alleMeldekortMedPeriode(sisteMeldekort.get(), alleMeldekort);
        if (alleMeldekortMedPeriode.size() > 1) {
            return finnMeldekortSomGjelderForVedtak(alleMeldekortMedPeriode, sisteVedtak).isPresent();
        }

        return true;
    }

    public static BigDecimal getUtbetalingsGrad(YtelseDto ytelse, YtelseAnvistDto anvist) {
        return mapTilNormertUtbetalingsprosent(ytelse, anvist).normertUtbetalingsprosent().tilNormalisertGrad();
    }

    private static List<UtbetalingMedNormertUtbetalingsprosent> finnAlleMeldekort(YtelseFilterDto ytelseFilter, Set<YtelseType> ytelseTyper){
        return ytelseFilter.getFiltrertYtelser().stream()
            .filter(ytelse -> ytelseTyper.contains(ytelse.getYtelseType()))
            .flatMap(ytelse -> ytelse.getYtelseAnvist().stream().map(u -> mapTilNormertUtbetalingsprosent(ytelse, u)))
            .toList();
    }

    private static UtbetalingMedNormertUtbetalingsprosent mapTilNormertUtbetalingsprosent(YtelseDto ytelse, YtelseAnvistDto utbetaling) {
        // Etterhvert så må denne dele på 2 kun dersom kilde == ARENA - default er 0..100%
        var normert = utbetaling.getUtbetalingsgradProsent()
            .map(Stillingsprosent::verdi)
            .map(up -> ytelse.harKildeKelvinEllerDpSak() ? up : up.divide(ARENA_DIVISOR, up.scale() + 1, RoundingMode.HALF_UP))
            .map(Stillingsprosent::fra)
            .orElse(Stillingsprosent.HUNDRED);
        return new UtbetalingMedNormertUtbetalingsprosent(utbetaling, Stillingsprosent.HUNDRED.compareTo(normert) == 0, normert);
    }


    private static List<UtbetalingMedNormertUtbetalingsprosent> alleMeldekortMedPeriode(UtbetalingMedNormertUtbetalingsprosent siste,
                                                                                        List<UtbetalingMedNormertUtbetalingsprosent> alleMeldekort) {
        return alleMeldekort.stream()
            .filter(meldekort -> Objects.equals(meldekort.utbetaling().getAnvistFOM(), siste.utbetaling().getAnvistFOM()))
            .filter(meldekort -> Objects.equals(meldekort.utbetaling().getAnvistTOM(), siste.utbetaling().getAnvistTOM()))
            .toList();
    }

    private static Optional<UtbetalingMedNormertUtbetalingsprosent> finnMeldekortSomGjelderForVedtak(List<UtbetalingMedNormertUtbetalingsprosent> meldekort, YtelseDto sisteVedtak) {
        return meldekort.stream().filter(m -> matcherMeldekortFraSisteVedtak(m.utbetaling(), sisteVedtak)).findFirst();
    }

    private static boolean matcherMeldekortFraSisteVedtak(YtelseAnvistDto meldekort, YtelseDto sisteVedtak) {
        return sisteVedtak.getYtelseAnvist().stream().anyMatch(ya -> Objects.equals(ya, meldekort));
    }

}
