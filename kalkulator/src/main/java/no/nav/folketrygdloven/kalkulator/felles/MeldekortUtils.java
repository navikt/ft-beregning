package no.nav.folketrygdloven.kalkulator.felles;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.konfig.KonfigTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

public class MeldekortUtils {

    public static final BigDecimal MAX_UTBETALING_PROSENT_AAP_DAG = BigDecimal.valueOf(200);

    private MeldekortUtils() {}

    public static Optional<YtelseDto> sisteVedtakFørStpForType(YtelseFilterDto ytelseFilter, LocalDate skjæringstidspunkt, Set<YtelseType> ytelseTyper) {
        return ytelseFilter.getFiltrertYtelser().stream()
            .filter(ytelse -> ytelseTyper.contains(ytelse.getYtelseType()))
            .filter(ytelse -> !skjæringstidspunkt.isBefore(ytelse.getPeriode().getFomDato()))
            .max(Comparator.comparing(YtelseDto::getPeriode).thenComparing(ytelse -> ytelse.getPeriode().getTomDato()));
    }

    public static Optional<YtelseAnvistDto> sisteHeleMeldekortFørStp(YtelseFilterDto ytelseFilter,
                                                                     YtelseDto sisteVedtak,
                                                                     LocalDate skjæringstidspunkt,
                                                                     Set<YtelseType> ytelseTyper) {
        final LocalDate sisteVedtakFom = sisteVedtak.getPeriode().getFomDato();

        List<YtelseAnvistDto> alleMeldekort = finnAlleMeldekort(ytelseFilter, ytelseTyper);

        Optional<YtelseAnvistDto> sisteMeldekort = alleMeldekort.stream()
            .filter(ytelseAnvist -> sisteVedtakFom.minus(KonfigTjeneste.getMeldekortPeriode()).isBefore(ytelseAnvist.getAnvistTOM()))
            .filter(ytelseAnvist -> skjæringstidspunkt.isAfter(ytelseAnvist.getAnvistTOM()))
            .max(Comparator.comparing(YtelseAnvistDto::getAnvistFOM));

        if (sisteMeldekort.isEmpty()) {
            return Optional.empty();
        }

        // Vi er nødt til å sjekke om vi har flere meldekort med samme periode
        List<YtelseAnvistDto> alleMeldekortMedPeriode = alleMeldekortMedPeriode(sisteMeldekort.get().getAnvistFOM(), sisteMeldekort.get().getAnvistTOM(), alleMeldekort);

        if (alleMeldekortMedPeriode.size() > 1) {
            return finnMeldekortSomGjelderForVedtak(alleMeldekortMedPeriode, sisteVedtak);
        }

        return sisteMeldekort;

    }

    public static Optional<YtelseAnvistDto> finnMeldekortSomInkludererGittDato(YtelseFilterDto ytelseFilter, YtelseDto sisteVedtak, Set<YtelseType> ytelseTyper, LocalDate gittDato) {
        List<YtelseAnvistDto> alleMeldekort = finnAlleMeldekort(ytelseFilter, ytelseTyper);

        Optional<YtelseAnvistDto> sisteMeldekort = alleMeldekort.stream()
                .filter(ytelseAnvist -> !gittDato.isBefore(ytelseAnvist.getAnvistFOM()))
                .filter(ytelseAnvist -> !gittDato.isAfter(ytelseAnvist.getAnvistTOM()))
                .max(Comparator.comparing(YtelseAnvistDto::getAnvistFOM));

        if (sisteMeldekort.isEmpty()) {
            return Optional.empty();
        }

        List<YtelseAnvistDto> alleMeldekortMedPeriode = alleMeldekortMedPeriode(sisteMeldekort.get().getAnvistFOM(), sisteMeldekort.get().getAnvistTOM(), alleMeldekort);
        if (alleMeldekortMedPeriode.size() > 1) {
            return finnMeldekortSomGjelderForVedtak(alleMeldekortMedPeriode, sisteVedtak);
        }

        return sisteMeldekort;
    }

    public static List<YtelseAnvistDto> finnAlleMeldekort(YtelseFilterDto ytelseFilter, Set<YtelseType> ytelseTyper){
        return ytelseFilter.getFiltrertYtelser().stream()
                .filter(ytelse -> ytelseTyper.contains(ytelse.getYtelseType()))
                .flatMap(ytelse -> ytelse.getYtelseAnvist().stream()).collect(Collectors.toList());
    }


    private static List<YtelseAnvistDto> alleMeldekortMedPeriode(LocalDate anvistFOM, LocalDate anvistTOM, List<YtelseAnvistDto> alleMeldekort) {
        return alleMeldekort.stream()
            .filter(meldekort -> Objects.equals(meldekort.getAnvistFOM(), anvistFOM))
            .filter(meldekort -> Objects.equals(meldekort.getAnvistTOM(), anvistTOM))
            .collect(Collectors.toList());
    }

    private static Optional<YtelseAnvistDto> finnMeldekortSomGjelderForVedtak(List<YtelseAnvistDto> meldekort, YtelseDto sisteVedtak) {
        return meldekort.stream().filter(m -> matcherMeldekortFraSisteVedtak(m, sisteVedtak)).findFirst();
    }

    private static boolean matcherMeldekortFraSisteVedtak(YtelseAnvistDto meldekort, YtelseDto sisteVedtak) {
        return sisteVedtak.getYtelseAnvist().stream().anyMatch(ya -> Objects.equals(ya, meldekort));
    }

}
