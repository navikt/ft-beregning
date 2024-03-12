package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.periodisering;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;

public final class FinnAnsettelsesPeriode {

    private FinnAnsettelsesPeriode() {
        // skjul public constructor
    }

    public static Optional<Periode> finnMinMaksPeriode(Collection<AktivitetsAvtaleDto> ansettelsesPerioder, LocalDate skjæringstidspunkt) {
        return Optional.ofNullable(getMinMaksPeriode(ansettelsesPerioder, skjæringstidspunkt));
    }


    /**
     * Finner min-max uavhengig av skjæringstidspunkt
     *
     * @param ansettelsesPerioder Ansettelsesperioder
     * @return Periode {@link Periode}
     */
    public static Optional<Periode> getMinMaksPeriode(Collection<AktivitetsAvtaleDto> ansettelsesPerioder) {
        var arbeidsperiodeFom = ansettelsesPerioder
                .stream()
                .map(a -> a.getPeriode().getFomDato())
                .min(Comparator.naturalOrder());
        var arbeidsperiodeTom = ansettelsesPerioder
                .stream()
                .map(a -> a.getPeriode().getTomDato())
                .max(Comparator.naturalOrder());
        if (arbeidsperiodeFom.isEmpty() || arbeidsperiodeTom.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Periode.of(arbeidsperiodeFom.get(), arbeidsperiodeTom.get()));
    }

    /**
     * Forventer at skjæringstidspunktet ligger i en av ansettelses periodene
     *
     * @param ansettelsesPerioder Ansettelsesperioder
     * @param skjæringstidspunkt  Skjæringstidspunkt
     * @return Periode {@link Periode}
     */
    public static Periode getMinMaksPeriode(Collection<AktivitetsAvtaleDto> ansettelsesPerioder, LocalDate skjæringstidspunkt) {
        List<AktivitetsAvtaleDto> perioderSomSlutterEtterStp = ansettelsesPerioder
                .stream()
                .filter(ap -> !ap.getPeriode().getTomDato().isBefore(BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunkt)))
                .collect(Collectors.toList());
        return getMinMaksPeriode(perioderSomSlutterEtterStp).orElse(null);
    }
}
