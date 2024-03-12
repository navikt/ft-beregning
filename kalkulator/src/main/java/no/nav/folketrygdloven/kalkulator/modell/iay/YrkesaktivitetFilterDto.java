package no.nav.folketrygdloven.kalkulator.modell.iay;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidsforholdHandlingType;

/**
 * Brukt til å filtrere registrerte yrkesaktiviteter, overstyrte arbeidsforhold og frilans arbeidsforhold etter skjæringstidspunkt.
 * Håndterer både registrerte (register) opplysninger, saksbehandlers data (fra opptjening) og overstyringer.
 */
public class YrkesaktivitetFilterDto {

    private ArbeidsforholdInformasjonDto arbeidsforholdOverstyringer;
    private LocalDate skjæringstidspunkt;
    private Boolean ventreSideAvSkjæringstidspunkt;
    private Collection<YrkesaktivitetDto> yrkesaktiviteter;

    public YrkesaktivitetFilterDto(ArbeidsforholdInformasjonDto overstyringer, Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        this.arbeidsforholdOverstyringer = overstyringer;
        this.yrkesaktiviteter = yrkesaktiviteter;
    }

    public YrkesaktivitetFilterDto(ArbeidsforholdInformasjonDto arbeidsforholdInformasjon, AktørArbeidDto arbeid) {
        this(arbeidsforholdInformasjon, arbeid.hentAlleYrkesaktiviteter());
    }

    public YrkesaktivitetFilterDto(Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon, Optional<AktørArbeidDto> aktørArbeid) {
        this(arbeidsforholdInformasjon == null ? null : arbeidsforholdInformasjon.orElse(null), aktørArbeid.map(AktørArbeidDto::hentAlleYrkesaktiviteter).orElse(Collections.emptyList()));
    }

    public YrkesaktivitetFilterDto(Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon, AktørArbeidDto aktørArbeid) {
        this(arbeidsforholdInformasjon == null ? null : arbeidsforholdInformasjon.orElse(null), aktørArbeid == null ? Collections.emptyList() : aktørArbeid.hentAlleYrkesaktiviteter());
    }

    public YrkesaktivitetFilterDto(ArbeidsforholdInformasjonDto arbeidsforholdInformasjon, YrkesaktivitetDto yrkesaktivitet) {
        this(arbeidsforholdInformasjon, yrkesaktivitet == null ? Collections.emptyList() : List.of(yrkesaktivitet));
    }

    public YrkesaktivitetFilterDto(Optional<ArbeidsforholdInformasjonDto> arbeidsforholdInformasjon, YrkesaktivitetDto yrkesaktivitet) {
        this(arbeidsforholdInformasjon.orElse(null), yrkesaktivitet == null ? Collections.emptyList() : List.of(yrkesaktivitet));
    }

    /**
     * Tar inn angitte yrkesaktiviteter, uten hensyn til overstyringer.
     */
    public YrkesaktivitetFilterDto(Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        this(null, yrkesaktiviteter);
    }

    public YrkesaktivitetFilterDto(YrkesaktivitetDto yrkesaktivitet) {
        this(null, List.of(yrkesaktivitet));
    }

    public Collection<AktivitetsAvtaleDto> getAktivitetsAvtalerForArbeid(YrkesaktivitetDto ya) {
        Collection<AktivitetsAvtaleDto> aktivitetsAvtaler = filterAktivitetsAvtaleOverstyring(ya, internGetAktivitetsAvtalerForArbeid(ya));
        return aktivitetsAvtaler;
    }

    private Set<AktivitetsAvtaleDto> internGetAktivitetsAvtalerForArbeid(YrkesaktivitetDto ya) {
        return ya.getAlleAktivitetsAvtaler().stream()
                .filter(av -> (!ya.erArbeidsforhold() || !av.erAnsettelsesPeriode()))
                .filter(this::skalMedEtterSkjæringstidspunktVurdering)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Collection<YrkesaktivitetDto> getFrilansOppdrag() {
        return getAlleYrkesaktiviteter().stream()
                .filter(this::erFrilansOppdrag)
                .filter(it -> !getAktivitetsAvtalerForArbeid(it).isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    public Collection<YrkesaktivitetDto> getYrkesaktiviteter() {
        var ya = getYrkesaktiviteterInklusiveFiktive().stream()
                .filter(this::erIkkeFrilansOppdrag)
                .filter(this::skalBrukes)
                .filter(it -> (erArbeidsforholdOgStarterPåRettSideAvSkjæringstidspunkt(it) || !getAktivitetsAvtalerForArbeid(it).isEmpty()))
                .collect(Collectors.toUnmodifiableSet());
        return ya;
    }

    /**
     * Collection av aktiviteter filtrert iht ArbeidsforholdInformasjon.
     * Aktiviteter hvor overstyring har satt ArbeidsforholdHandlingType til INNTEKT_IKKE_MED_I_BG filtreres ut.
     *
     * @return Liste av {@link YrkesaktivitetDto}
     */
    public Collection<YrkesaktivitetDto> getYrkesaktiviteterForBeregning() {
        return getYrkesaktiviteterInklusiveFiktive().stream()
                .filter(this::erIkkeFrilansOppdrag)
                .filter(this::skalBrukesIBeregning)
                .filter(it -> (erArbeidsforholdOgStarterPåRettSideAvSkjæringstidspunkt(it) || !getAktivitetsAvtalerForArbeid(it).isEmpty()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private List<YrkesaktivitetDto> arbeidsforholdLagtTilAvSaksbehandler() {
        List<YrkesaktivitetDto> fiktiveArbeidsforhold = new ArrayList<>();
        if (arbeidsforholdOverstyringer != null) {
            var overstyringer = arbeidsforholdOverstyringer.getOverstyringer()
                    .stream()
                    .filter(os -> os.getStillingsprosent() != null && os.getStillingsprosent().verdi() != null)
                    .collect(Collectors.toList());
            for (var arbeidsforholdOverstyringEntitet : overstyringer) {
                var yrkesaktivitetBuilder = YrkesaktivitetDtoBuilder.oppdatere(Optional.empty())
                        .medArbeidsgiver(arbeidsforholdOverstyringEntitet.getArbeidsgiver())
                        .medArbeidType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)
                        .medArbeidsforholdId(arbeidsforholdOverstyringEntitet.getArbeidsforholdRef());
                var arbeidsforholdOverstyrtePerioder = arbeidsforholdOverstyringEntitet
                        .getArbeidsforholdOverstyrtePerioder();
                for (var arbeidsforholdOverstyrtePeriode : arbeidsforholdOverstyrtePerioder) {
                    var aktivitetsAvtaleBuilder = yrkesaktivitetBuilder
                            .getAktivitetsAvtaleBuilder(arbeidsforholdOverstyrtePeriode.getOverstyrtePeriode(), true);
                    yrkesaktivitetBuilder.leggTilAktivitetsAvtale(aktivitetsAvtaleBuilder);
                }
                var yrkesaktivitetEntitet = yrkesaktivitetBuilder.build();
                // yrkesaktivitetEntitet.setAktørArbeid(this); // OJR/FC: er samstemte om at denne ikke trengs
                fiktiveArbeidsforhold.add(yrkesaktivitetEntitet);
            }
        }
        return fiktiveArbeidsforhold;
    }

    private boolean erArbeidsforholdOgStarterPåRettSideAvSkjæringstidspunkt(YrkesaktivitetDto it) {
        boolean retval = it.erArbeidsforhold()
                && getAnsettelsesPerioder(it).stream().anyMatch(ap -> skalMedEtterSkjæringstidspunktVurdering(ap));
        return retval;
    }

    private boolean erFrilansOppdrag(YrkesaktivitetDto aktivitet) {
        return ArbeidType.FRILANSER_OPPDRAGSTAKER.equals(aktivitet.getArbeidType());
    }

    private boolean erIkkeFrilansOppdrag(YrkesaktivitetDto aktivitet) {
        return !ArbeidType.FRILANSER_OPPDRAGSTAKER.equals(aktivitet.getArbeidType());
    }

    private Set<YrkesaktivitetDto> getYrkesaktiviteterInklusiveFiktive() {
        var aktiviteter = new HashSet<>(getAlleYrkesaktiviteter());
        aktiviteter.addAll(arbeidsforholdLagtTilAvSaksbehandler());
        return Collections.unmodifiableSet(aktiviteter);
    }

    private Collection<YrkesaktivitetDto> getAlleYrkesaktiviteter() {
        return yrkesaktiviteter == null ? Collections.emptyList() : Collections.unmodifiableCollection(yrkesaktiviteter);
    }

    private boolean skalBrukes(YrkesaktivitetDto entitet) {
        return arbeidsforholdOverstyringer == null || arbeidsforholdOverstyringer.getOverstyringer()
                .stream()
                .noneMatch(ov -> entitet.gjelderFor(ov.getArbeidsgiver(), ov.getArbeidsforholdRef())
                        && Objects.equals(ArbeidsforholdHandlingType.IKKE_BRUK, ov.getHandling()));
    }

    private boolean skalBrukesIBeregning(YrkesaktivitetDto entitet) {
        return arbeidsforholdOverstyringer == null || arbeidsforholdOverstyringer.getOverstyringer().stream()
                .noneMatch(ov -> entitet.gjelderFor(ov.getArbeidsgiver(), ov.getArbeidsforholdRef()) &&
                        (Objects.equals(ArbeidsforholdHandlingType.INNTEKT_IKKE_MED_I_BG, ov.getHandling()) ||
                                Objects.equals(ArbeidsforholdHandlingType.IKKE_BRUK, ov.getHandling())));
    }

    public YrkesaktivitetFilterDto etter(LocalDate skjæringstidspunkt) {
        var filter = new YrkesaktivitetFilterDto(arbeidsforholdOverstyringer, getAlleYrkesaktiviteter());
        filter.skjæringstidspunkt = skjæringstidspunkt;
        filter.ventreSideAvSkjæringstidspunkt = skjæringstidspunkt == null;
        return filter;
    }

    public YrkesaktivitetFilterDto før(LocalDate skjæringstidspunkt) {
        var filter = new YrkesaktivitetFilterDto(arbeidsforholdOverstyringer, getAlleYrkesaktiviteter());
        filter.skjæringstidspunkt = skjæringstidspunkt;
        filter.ventreSideAvSkjæringstidspunkt = (skjæringstidspunkt != null);
        return filter;
    }

    boolean skalMedEtterSkjæringstidspunktVurdering(AktivitetsAvtaleDto ap) {

        if (skjæringstidspunkt != null) {
            if (ventreSideAvSkjæringstidspunkt) {
                return ap.getPeriode().getFomDato().isBefore(skjæringstidspunkt);
            } else {
                return ap.getPeriode().getFomDato().isAfter(skjæringstidspunkt.minusDays(1)) ||
                        ap.getPeriode().getFomDato().isBefore(skjæringstidspunkt) && ap.getPeriode().getTomDato().isAfter(skjæringstidspunkt.minusDays(1));
            }
        }
        return true;
    }

    private Collection<AktivitetsAvtaleDto> filterAktivitetsAvtaleOverstyring(YrkesaktivitetDto ya, Collection<AktivitetsAvtaleDto> yaAvtaler) {

        Optional<ArbeidsforholdOverstyringDto> overstyringOpt = finnMatchendeOverstyring(ya);

        if (overstyringOpt.isPresent()) {
            return overstyrYrkesaktivitet(overstyringOpt.get(), yaAvtaler);
        } else {
            return yaAvtaler;
        }
    }

    Collection<AktivitetsAvtaleDto> overstyrYrkesaktivitet(ArbeidsforholdOverstyringDto overstyring, Collection<AktivitetsAvtaleDto> yaAvtaler) {
        ArbeidsforholdHandlingType handling = overstyring.getHandling();

        List<ArbeidsforholdOverstyrtePerioderDto> overstyrtePerioder = overstyring.getArbeidsforholdOverstyrtePerioder();
        if (handling.erPeriodeOverstyrt() && !overstyrtePerioder.isEmpty()) {
            Set<AktivitetsAvtaleDto> avtaler = new LinkedHashSet<>();
            overstyrtePerioder.forEach(overstyrtPeriode -> yaAvtaler.stream()
                    .filter(AktivitetsAvtaleDto::erAnsettelsesPeriode)
                    .filter(aa -> TIDENES_ENDE.equals(aa.getPeriodeUtenOverstyring().getTomDato()))
                    .filter(aa -> overstyrtPeriode.getOverstyrtePeriode().getFomDato().isEqual(aa.getPeriodeUtenOverstyring().getFomDato()))
                    .forEach(avtale -> avtaler.add(new AktivitetsAvtaleDto(avtale, Intervall.fraOgMedTilOgMed(overstyrtPeriode.getOverstyrtePeriode().getFomDato(), overstyrtPeriode.getOverstyrtePeriode().getTomDato())))));

            // legg til resten, bruk av set hindrer oss i å legge dobbelt.
            yaAvtaler.stream().forEach(avtale -> avtaler.add(new AktivitetsAvtaleDto(avtale)));
            return avtaler;
        } else {
            // ingen overstyring, returner samme
            return yaAvtaler;
        }

    }

    private Optional<ArbeidsforholdOverstyringDto> finnMatchendeOverstyring(YrkesaktivitetDto ya) {
        if (arbeidsforholdOverstyringer == null) {
            return Optional.empty(); // ikke initialisert, så kan ikke ha overstyringer
        }
        List<ArbeidsforholdOverstyringDto> overstyringer = arbeidsforholdOverstyringer.getOverstyringer();
        if (overstyringer.isEmpty()) {
            return Optional.empty();
        }
        return overstyringer.stream()
                .filter(os -> ya.gjelderFor(os.getArbeidsgiver(), os.getArbeidsforholdRef()))
                .findFirst();
    }

    /**
     * Gir alle ansettelsesperioden for et arbeidsforhold.
     * <p>
     * NB! Gjelder kun arbeidsforhold.
     *
     * @return perioden
     */
    public List<AktivitetsAvtaleDto> getAnsettelsesPerioder(YrkesaktivitetDto ya) {
        if (ya.erArbeidsforhold()) {
            List<AktivitetsAvtaleDto> ansettelsesAvtaler = ya.getAlleAktivitetsAvtaler().stream()
                    .filter(AktivitetsAvtaleDto::erAnsettelsesPeriode)
                    .collect(Collectors.toList());
            List<AktivitetsAvtaleDto> filtrert = List.copyOf(filterAktivitetsAvtaleOverstyring(ya, ansettelsesAvtaler));
            return filtrert;
        }
        return Collections.emptyList();
    }

    /**
     * Gir ansettelsesperioder for angitte arbeidsforhold.
     *
     * @see #getAnsettelsesPerioder(YrkesaktivitetDto)
     */
    public Collection<AktivitetsAvtaleDto> getAnsettelsesPerioder(Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        var aktivitetsavtaler = yrkesaktiviteter.stream().flatMap(ya -> getAnsettelsesPerioder(ya).stream()).collect(Collectors.toList());
        return aktivitetsavtaler;
    }

    /**
     * Gir alle ansettelsesperioder for filteret, inklusiv fiktive fra saksbehandler hvis konfigurert på filteret.
     *
     * @see #getAnsettelsesPerioder(YrkesaktivitetDto)
     */
    public Collection<AktivitetsAvtaleDto> getAnsettelsesPerioder() {
        var ansettelsesPerioder = getYrkesaktiviteterInklusiveFiktive().stream().flatMap(ya -> getAnsettelsesPerioder(ya).stream()).collect(Collectors.toList());
        return ansettelsesPerioder;
    }


}
