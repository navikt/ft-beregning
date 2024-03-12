package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektskildeType;
import no.nav.folketrygdloven.kalkulus.kodeverk.InntektspostType;


/**
 * Filter for å hente inntekter og inntektsposter fra grunnlag. Tilbyr håndtering av skjæringstidspunkt og filtereing på inntektskilder slik
 * at en ikke trenger å implementere selv navigering av modellen.
 */
public class InntektFilterDto {
    public static final InntektFilterDto EMPTY = new InntektFilterDto(Collections.emptyList());

    private final Collection<InntektDto> inntekter;
    private final LocalDate skjæringstidspunkt;
    private final Boolean venstreSideASkjæringstidspunkt;

    private BiPredicate<InntektDto, InntektspostDto> inntektspostFilter;

    public InntektFilterDto(AktørInntektDto aktørInntekt) {
        this(aktørInntekt.getInntekt(), null, null);
    }

    private InntektFilterDto(Collection<InntektDto> inntekter) {
        this(inntekter, null, null);
    }

    private InntektFilterDto(Collection<InntektDto> inntekter, LocalDate skjæringstidspunkt, Boolean venstreSideASkjæringstidspunkt) {
        this.inntekter = inntekter == null ? Collections.emptyList() : inntekter;
        this.skjæringstidspunkt = skjæringstidspunkt;
        this.venstreSideASkjæringstidspunkt = venstreSideASkjæringstidspunkt;
    }

    public InntektFilterDto(Optional<AktørInntektDto> aktørInntekt) {
        this(aktørInntekt.isPresent() ? aktørInntekt.get().getInntekt() : Collections.emptyList());
    }

    public InntektFilterDto etter(LocalDate skjæringstidspunkt) {
        return copyWith(this.inntekter, skjæringstidspunkt, false);
    }

    public boolean isEmpty() {
        return inntekter.isEmpty();
    }

    public InntektFilterDto filter(Arbeidsgiver arbeidsgiver) {
        var innt = inntekter.stream().filter(i -> Objects.equals(arbeidsgiver, i.getArbeidsgiver())).collect(Collectors.toList());
        return copyWith(innt, skjæringstidspunkt, venstreSideASkjæringstidspunkt);
    }

    public InntektFilterDto filter(InntektskildeType kilde) {
        return copyWith(getAlleInntekter(kilde), skjæringstidspunkt, venstreSideASkjæringstidspunkt);
    }

    public InntektFilterDto filter(InntektspostType inntektspostType) {
        return filter(Set.of(inntektspostType));
    }

    public InntektFilterDto filter(InntektspostType... inntektspostTyper) {
        return filter(Set.of(inntektspostTyper));
    }

    public InntektFilterDto filter(Set<InntektspostType> typer) {
        return filter((inntekt, inntektspost) -> typer.contains(inntektspost.getInntektspostType()));
    }

    public InntektFilterDto filterBeregnetSkatt() {
        return copyWith(getAlleInntektBeregnetSkatt(), skjæringstidspunkt, venstreSideASkjæringstidspunkt);
    }

    public InntektFilterDto filterBeregningsgrunnlag() {
        return copyWith(getAlleInntektBeregningsgrunnlag(), skjæringstidspunkt, venstreSideASkjæringstidspunkt);
    }

    public InntektFilterDto filterSammenligningsgrunnlag() {
        return copyWith(getAlleInntektSammenligningsgrunnlag(), skjæringstidspunkt, venstreSideASkjæringstidspunkt);
    }

    public InntektFilterDto før(LocalDate skjæringstidspunkt) {
        return copyWith(this.inntekter, skjæringstidspunkt, true);
    }

    private List<InntektDto> getAlleInntektBeregnetSkatt() {
        return getAlleInntekter(InntektskildeType.SIGRUN);
    }

    private List<InntektDto> getAlleInntektBeregningsgrunnlag() {
        return getAlleInntekter(InntektskildeType.INNTEKT_BEREGNING);
    }

    private List<InntektDto> getAlleInntekter(InntektskildeType kilde) {
        return inntekter.stream()
            .filter(it -> kilde == null || kilde.equals(it.getInntektsKilde()))
            .collect(Collectors.toUnmodifiableList());
    }

    private List<InntektDto> getAlleInntekter() {
        return getAlleInntekter(null);
    }

    public List<InntektDto> getAlleInntektSammenligningsgrunnlag() {
        return getAlleInntekter(InntektskildeType.INNTEKT_SAMMENLIGNING);
    }

    /**
     * Get inntektsposter - filtrert for skjæringstidspunkt hvis satt på filter.
     */
    public Collection<InntektspostDto> getFiltrertInntektsposter() {
        return getInntektsposter((InntektskildeType) null);
    }

    /**
     * Get inntektsposter - filtrert for skjæringstidspunkt, inntektsposttype, etc hvis satt på filter.
     */
    private Collection<InntektspostDto> getInntektsposter(InntektskildeType kilde) {
        Collection<InntektspostDto> inntektsposter = getAlleInntekter(null).stream().filter(i -> kilde == null || kilde.equals(i.getInntektsKilde()))
            .flatMap(i -> i.getAlleInntektsposter().stream().filter(ip -> filtrerInntektspost(i, ip)))
            .collect(Collectors.toList());
        return Collections.unmodifiableCollection(inntektsposter);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
            + "<inntekter(" + inntekter.size() + ")"
            + (skjæringstidspunkt == null ? "" : ", skjæringstidspunkt=" + skjæringstidspunkt)
            + (venstreSideASkjæringstidspunkt == null ? "" : ", venstreSideASkjæringstidspunkt=" + venstreSideASkjæringstidspunkt)
            + ">";
    }

    private boolean filtrerInntektspost(InntektDto inntekt, InntektspostDto ip) {
        return (inntektspostFilter == null || inntektspostFilter.test(inntekt, ip))
            && skalMedEtterSkjæringstidspunktVurdering(ip);
    }

    private Collection<InntektspostDto> getFiltrertInntektsposter(InntektDto inntekt) {
        Collection<InntektspostDto> inntektsposter = inntekt.getAlleInntektsposter().stream().filter(ip -> filtrerInntektspost(inntekt, ip))
            .collect(Collectors.toList());
        return Collections.unmodifiableCollection(inntektsposter);
    }

    private boolean skalMedEtterSkjæringstidspunktVurdering(InntektspostDto inntektspost) {
        if (inntektspost == null) {
            return false;
        }
        if (skjæringstidspunkt != null) {
            Intervall periode = inntektspost.getPeriode();
            if (venstreSideASkjæringstidspunkt) {
                return periode.getFomDato().isBefore(skjæringstidspunkt.plusDays(1));
            } else {
                return periode.getFomDato().isAfter(skjæringstidspunkt) ||
                    periode.getFomDato().isBefore(skjæringstidspunkt.plusDays(1)) && periode.getTomDato().isAfter(skjæringstidspunkt);
            }
        }
        return true;
    }

    /**
     * Appliserer angitt funksjon til hver inntekt og for inntekts inntektsposter som matcher dette filteret.
     */
    public void forFilter(BiConsumer<InntektDto, Collection<InntektspostDto>> consumer) {
        getAlleInntekter().forEach(i -> {
            var inntektsposterFiltrert = getFiltrertInntektsposter(i).stream().filter(ip -> filtrerInntektspost(i, ip)).collect(Collectors.toList());
            consumer.accept(i, inntektsposterFiltrert);
        });
    }

    public InntektFilterDto filter(Predicate<InntektDto> filterFunc) {
        return copyWith(getAlleInntekter().stream().filter(filterFunc).collect(Collectors.toList()), skjæringstidspunkt, venstreSideASkjæringstidspunkt);
    }

    public InntektFilterDto filter(BiPredicate<InntektDto, InntektspostDto> filterFunc) {
        var copy = copyWith(getAlleInntekter().stream()
            .filter(i -> i.getAlleInntektsposter().stream().anyMatch(ip -> filterFunc.test(i, ip)))
            .collect(Collectors.toList()), skjæringstidspunkt, venstreSideASkjæringstidspunkt);

        if (copy.inntektspostFilter == null)
            copy.inntektspostFilter = filterFunc;
        else
            copy.inntektspostFilter = (inntekt, inntektspost) -> filterFunc.test(inntekt, inntektspost) && this.inntektspostFilter.test(inntekt, inntektspost);
        return copy;
    }

    private InntektFilterDto copyWith(Collection<InntektDto> inntekter, LocalDate skjæringstidspunkt, Boolean venstreSideASkjæringstidspunkt) {
        var copy = new InntektFilterDto(inntekter, skjæringstidspunkt, venstreSideASkjæringstidspunkt);
        copy.inntektspostFilter = this.inntektspostFilter;
        return copy;
    }

}
