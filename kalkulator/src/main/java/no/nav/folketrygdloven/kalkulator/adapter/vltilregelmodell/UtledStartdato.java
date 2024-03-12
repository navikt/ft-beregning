package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell;

import java.time.LocalDate;
import java.util.Comparator;

import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

class UtledStartdato {

    /** Finner siste startdato før skjæringstidspunktet for arbeidsforholdet.
     *
     * @param arbeidsgiver Arbeidsgiver
     * @param arbeidsforholdRef arbeidsforholdRef for andel/aktivitet (kan være null)
     * @param iayGrunnlag Iaygrunnlag
     * @param stp Skjæringstidspunkt
     * @return Siste startdato for arbeidsforhold før stp, null dersom ikke finnes
     */
    static LocalDate utledStartdato(Arbeidsgiver arbeidsgiver, String arbeidsforholdRef, InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate stp) {
        var ansettelsessegmenter = iayGrunnlag.getAktørArbeidFraRegister().stream()
                .flatMap(aar -> aar.hentAlleYrkesaktiviteter().stream())
                .filter(ya -> ya.gjelderFor(arbeidsgiver, InternArbeidsforholdRefDto.ref(arbeidsforholdRef)))
                .flatMap(ya -> ya.getAlleAnsettelsesperioder().stream())
                .map(AktivitetsAvtaleDto::getPeriode)
                .map(p -> new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), true))
                .toList();

        var sammenslåttTidslinje = new LocalDateTimeline<>(ansettelsessegmenter, StandardCombinators::alwaysTrueForMatch)
                .intersection(new LocalDateInterval(LocalDateInterval.TIDENES_BEGYNNELSE, stp.minusDays(1))).compress();

        return sammenslåttTidslinje.getLocalDateIntervals().stream().map(LocalDateInterval::getFomDato).max(Comparator.naturalOrder()).orElse(null);
    }

}
