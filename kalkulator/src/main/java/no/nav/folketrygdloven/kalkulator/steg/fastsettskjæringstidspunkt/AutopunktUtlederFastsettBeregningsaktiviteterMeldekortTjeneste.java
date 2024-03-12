package no.nav.folketrygdloven.kalkulator.steg.fastsettskjæringstidspunkt;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.BeregningstidspunktTjeneste;
import no.nav.folketrygdloven.kalkulator.felles.MeldekortUtils;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseAnvistDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YtelseFilterDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.YtelseType;

public class AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste {

    private AutopunktUtlederFastsettBeregningsaktiviteterMeldekortTjeneste() {
        // Skjuler default konstruktør
    }

    /**
     * Utleder om det skal ventes på siste meldekort for AAP for å kunne beregne riktig beregningsgrunnlag.
     * Skal vente om:
     * - Har annen aktivitetstatus enn AAP
     * - Har løpende AAP på skjæringstidspunktet
     * - Har sendt inn meldekort for AAP de siste 4 mnd før skjæringstidspunkt for opptjening
     *
     *
     * @param aktørYtelse aktørytelse for søker
     * @param dagensdato Dagens dato/ idag
     * @param skjæringstidspunkt Skjæringstidspunkt
     * @param arenaytelser Arenaytelser som skal vurderes
     * @return Optional som innholder ventefrist om autopunkt skal opprettes, Optional.empty ellers
     */
    public static Optional<LocalDate> skalVenteTilDatoPåMeldekortAAPellerDP(Optional<AktørYtelseDto> aktørYtelse, LocalDate dagensdato, LocalDate skjæringstidspunkt, Set<YtelseType> arenaytelser) {
        if (!harLøpendeVedtakOgSendtInnMeldekortNylig(aktørYtelse, skjæringstidspunkt, arenaytelser))
            return Optional.empty();

        if(erSisteMeldekortMottatt(aktørYtelse, skjæringstidspunkt, arenaytelser)){
            return Optional.empty();
        }

        return utledVenteFrist(skjæringstidspunkt, dagensdato);
    }

    private static boolean erSisteMeldekortMottatt(Optional<AktørYtelseDto> aktørYtelse, LocalDate skjæringstidspunkt, Set<YtelseType> arenaytelser) {
        var ytelseFilterVedtak = new YtelseFilterDto(aktørYtelse).før(skjæringstidspunkt);
        Optional<YtelseDto> nyligsteVedtak = MeldekortUtils.sisteVedtakFørStpForType(ytelseFilterVedtak, skjæringstidspunkt, arenaytelser);

        var ytelseFilterMeldekort = new YtelseFilterDto(aktørYtelse);


        if(BeregningstidspunktTjeneste.finnBeregningstidspunkt(skjæringstidspunkt).isBefore(skjæringstidspunkt) && opphørerYtelseDagenFørStp(nyligsteVedtak.get(), skjæringstidspunkt)){
            Optional<YtelseAnvistDto> meldekortOpphørtYtelse = MeldekortUtils.finnMeldekortSomInkludererGittDato(ytelseFilterMeldekort, nyligsteVedtak.get(),
                    Set.of(nyligsteVedtak.get().getYtelseType()), skjæringstidspunkt.minusDays(1));
            return meldekortOpphørtYtelse.isPresent();
        }

        Optional<YtelseAnvistDto> meldekortLøpendeYtelse = MeldekortUtils.finnMeldekortSomInkludererGittDato(ytelseFilterMeldekort, nyligsteVedtak.get(),
                Set.of(nyligsteVedtak.get().getYtelseType()), skjæringstidspunkt);
        return meldekortLøpendeYtelse.isPresent();
    }

    private static boolean harLøpendeVedtakOgSendtInnMeldekortNylig(Optional<AktørYtelseDto> aktørYtelse, LocalDate skjæringstidspunkt, Set<YtelseType> arenaytelseTyper) {
        List<YtelseDto> ytelser = getArenaytelserYtelser(aktørYtelse, skjæringstidspunkt, arenaytelseTyper);

        var skalSjekkeAAP = arenaytelseTyper.stream().anyMatch(y -> y.equals(YtelseType.ARBEIDSAVKLARINGSPENGER));
        var skalSjekkeDP = arenaytelseTyper.stream().anyMatch(y -> y.equals(YtelseType.DAGPENGER));

        boolean hattAAPSiste4Mnd = hattGittYtelseIGittPeriode(ytelser, skjæringstidspunkt.minusMonths(4).withDayOfMonth(1),
                YtelseType.ARBEIDSAVKLARINGSPENGER);
        Predicate<List<YtelseDto>> hattDPSiste10Mnd = it -> hattGittYtelseIGittPeriode(it, skjæringstidspunkt.minusMonths(10), YtelseType.DAGPENGER);

        if ((!skalSjekkeAAP || !hattAAPSiste4Mnd) && (!skalSjekkeDP || !hattDPSiste10Mnd.test(ytelser))) {
            return false;
        }

        var ytelseType = hattAAPSiste4Mnd ? YtelseType.ARBEIDSAVKLARINGSPENGER : YtelseType.DAGPENGER;
        return ytelser.stream()
            .filter(ytelse -> ytelseType.equals(ytelse.getYtelseType()))
            .anyMatch(ytelse -> ytelse.getPeriode().getFomDato().isBefore(skjæringstidspunkt)
                && !ytelse.getPeriode().getTomDato().isBefore(skjæringstidspunkt.minusDays(1)));
    }

    private static boolean hattGittYtelseIGittPeriode(List<YtelseDto> aapOgDPYtelser, LocalDate hattYtelseFom, YtelseType ytelseType) {
        return aapOgDPYtelser.stream()
            .filter(ytelse -> ytelseType.equals(ytelse.getYtelseType()))
            .flatMap(ytelse -> ytelse.getYtelseAnvist().stream())
            .anyMatch(ya -> !ya.getAnvistTOM().isBefore(hattYtelseFom));
    }

    private static List<YtelseDto> getArenaytelserYtelser(Optional<AktørYtelseDto> aktørYtelse, LocalDate skjæringstidspunkt, Set<YtelseType> arenaytelser) {
        var filter = new YtelseFilterDto(aktørYtelse).før(skjæringstidspunkt);
        return filter.getFiltrertYtelser().stream()
            .filter(ytelse -> arenaytelser.contains(ytelse.getYtelseType()))
            .collect(Collectors.toList());
    }

    private static Optional<LocalDate> utledVenteFrist(LocalDate skjæringstidspunktOpptjening, LocalDate dagensdato) {
        if (!dagensdato.isAfter(skjæringstidspunktOpptjening)) {
            return Optional.of(skjæringstidspunktOpptjening.plusDays(1));
        }
        if (!dagensdato.isAfter(skjæringstidspunktOpptjening.plusDays(14))) {
            return Optional.of(dagensdato.plusDays(1));
        }
        return Optional.empty();
    }

    private static boolean opphørerYtelseDagenFørStp(YtelseDto nyligsteVedtak, LocalDate skjæringstidspunkt){
        return nyligsteVedtak.getPeriode().getTomDato().isEqual(skjæringstidspunkt.minusDays(1));
    }
}
