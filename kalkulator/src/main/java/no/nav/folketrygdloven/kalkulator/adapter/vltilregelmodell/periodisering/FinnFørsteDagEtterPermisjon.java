package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonFilter;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public class FinnFørsteDagEtterPermisjon {
    private FinnFørsteDagEtterPermisjon() {
        // skjul public constructor
    }

    /**
     * @return Dersom det finnes en permisjon som gjelder på skjæringstidspunkt for beregning, returner første dag etter permisjonen.
     * Ellers: Returner første dag i ansettelsesperioden til arbeidsforholdet. Dette kan være enten før eller etter første uttaksdag.
     */
    public static Optional<LocalDate> finn(Collection<YrkesaktivitetDto> yrkesaktiviteter, Periode ansettelsesPeriode, LocalDate skjæringstidspunktBeregning, PermisjonFilter permisjonFilter) {
        var tidslinjeForPermisjon = yrkesaktiviteter.stream()
                .map(permisjonFilter::finnTidslinjeForPermisjonOver14Dager)
                .reduce((t1, t2) -> t1.intersection(t2, StandardCombinators::alwaysTrueForMatch))
                .orElse(LocalDateTimeline.empty());
        var beregningstidspunkt = BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunktBeregning);
        var harIkkePermisjonPåBeregningstidspunkt = tidslinjeForPermisjon.intersection(new LocalDateInterval(beregningstidspunkt, beregningstidspunkt))
                .isEmpty();
        if (harIkkePermisjonPåBeregningstidspunkt) {
            return Optional.of(ansettelsesPeriode.getFom());
        }
        var sisteDagMedPermisjon = tidslinjeForPermisjon.getLocalDateIntervals().stream()
                .filter(p -> p.contains(beregningstidspunkt))
                .map(LocalDateInterval::getTomDato)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        if (sisteDagMedPermisjon.equals(TIDENES_ENDE)) {
            return Optional.empty();
        }
        LocalDate dagenEtterBekreftetPermisjon = sisteDagMedPermisjon.plusDays(1);
        return Optional.of(dagenEtterBekreftetPermisjon);
    }
}
