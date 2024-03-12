package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

/**
 * Filter for å hente ytelser fra grunnlag. Tilbyr håndtering av skjæringstidspunkt og filtereing på ytelser slik
 * at en ikke trenger å implementere selv navigering av modellen.
 */
public class YtelseFilterDto {

    private final Collection<YtelseDto> ytelser;
    private final LocalDate skjæringstidspunkt;
    private final Boolean venstreSideASkjæringstidspunkt;

    private Predicate<YtelseDto> ytelseFilter;

    public YtelseFilterDto(Collection<YtelseDto> inntekter) {
        this(inntekter, null, null);
    }

    public YtelseFilterDto(Collection<YtelseDto> inntekter, LocalDate skjæringstidspunkt, Boolean venstreSideASkjæringstidspunkt) {
        this.ytelser = inntekter == null ? Collections.emptyList() : inntekter;
        this.skjæringstidspunkt = skjæringstidspunkt;
        this.venstreSideASkjæringstidspunkt = venstreSideASkjæringstidspunkt;
    }

    public YtelseFilterDto(Optional<AktørYtelseDto> aktørYtelse) {
        this(aktørYtelse.isPresent() ? aktørYtelse.get().getAlleYtelser() : Collections.emptyList());
    }

    public YtelseFilterDto etter(LocalDate skjæringstidspunkt) {
        return copyWith(this.ytelser, skjæringstidspunkt, false);
    }

    public boolean isEmpty() {
        return ytelser.isEmpty();
    }

    public YtelseFilterDto før(LocalDate skjæringstidspunkt) {
        return copyWith(this.ytelser, skjæringstidspunkt, true);
    }

    public List<YtelseDto> getAlleYtelser() {
        return List.copyOf(ytelser);
    }

    /**
     * Get ytelser - filtrert for skjæringstidspunkt hvis satt på filter.
     */
    public Collection<YtelseDto> getFiltrertYtelser() {
        return getFiltrertYtelser(getAlleYtelser());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
            + "<ytelser(" + ytelser.size() + ")"
            + (skjæringstidspunkt == null ? "" : ", skjæringstidspunkt=" + skjæringstidspunkt)
            + (venstreSideASkjæringstidspunkt == null ? "" : ", venstreSideASkjæringstidspunkt=" + venstreSideASkjæringstidspunkt)
            + ">";
    }

    /**
     * Get ytelser. Filtrer for skjæringstidspunkt, etc hvis definert
     */
    private Collection<YtelseDto> getFiltrertYtelser(Collection<YtelseDto> ytelser) {
        Collection<YtelseDto> resultat = ytelser.stream()
            .filter(yt -> (this.ytelseFilter == null || this.ytelseFilter.test(yt)) && skalMedEtterSkjæringstidspunktVurdering(yt))
            .collect(Collectors.toList());
        return Collections.unmodifiableCollection(resultat);
    }

    private boolean skalMedEtterSkjæringstidspunktVurdering(YtelseDto ytelse) {
        if (skjæringstidspunkt != null) {
            Intervall periode = ytelse.getPeriode();
            if (venstreSideASkjæringstidspunkt) {
                return periode.getFomDato().isBefore(skjæringstidspunkt.plusDays(1));
            } else {
                return periode.getFomDato().isAfter(skjæringstidspunkt) ||
                    periode.getFomDato().isBefore(skjæringstidspunkt.plusDays(1)) && periode.getTomDato().isAfter(skjæringstidspunkt);
            }
        }
        return true;
    }

    public YtelseFilterDto filter(Predicate<YtelseDto> filterFunc) {
        var copy = copyWith(getFiltrertYtelser().stream().filter(filterFunc).collect(Collectors.toList()), skjæringstidspunkt, venstreSideASkjæringstidspunkt);
        if (copy.ytelseFilter == null) {
            copy.ytelseFilter = filterFunc;
        } else {
            copy.ytelseFilter = (ytelse) -> filterFunc.test(ytelse) && this.ytelseFilter.test(ytelse);
        }
        return copy;
    }

    private YtelseFilterDto copyWith(Collection<YtelseDto> ytelser, LocalDate skjæringstidspunkt, Boolean venstreSideASkjæringstidspunkt) {
        var copy = new YtelseFilterDto(ytelser, skjæringstidspunkt, venstreSideASkjæringstidspunkt);
        copy.ytelseFilter = this.ytelseFilter;
        return copy;
    }

}
