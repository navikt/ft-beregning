package no.nav.folketrygdloven.kalkulator.adapter.util;

import static no.nav.fpsak.tidsserie.LocalDateInterval.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.iay.AktivitetsAvtaleDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.AktørYtelseDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetFilterDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonFilter;
import no.nav.folketrygdloven.kalkulator.modell.iay.permisjon.PermisjonPerYrkesaktivitet;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.typer.OrgNummer;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;


public class FinnArbeidsperiode {

    private final YrkesaktivitetFilterDto yrkesaktivitetFilter;

    public FinnArbeidsperiode(YrkesaktivitetFilterDto filter) {
        yrkesaktivitetFilter = filter;
    }

    public Intervall finnArbeidsperiode(Arbeidsgiver arbeidsgiver,
                                        InternArbeidsforholdRefDto iaRef,
                                        LocalDate skjæringstidspunkt) {
        var ansettelsesPerioder = yrkesaktivitetFilter.getYrkesaktiviteterForBeregning().stream()
                .filter(ya -> ya.gjelderFor(arbeidsgiver, iaRef))
                .map(yrkesaktivitetFilter::getAnsettelsesPerioder)
                .flatMap(Collection::stream)
                .filter(a -> !a.getPeriode().getFomDato().isAfter(skjæringstidspunkt))
                .collect(Collectors.toList());
        LocalDate arbeidsperiodeFom = ansettelsesPerioder.stream().map(a -> a.getPeriode().getFomDato()).min(LocalDate::compareTo).orElse(null);
        LocalDate arbeidsperiodeTom = ansettelsesPerioder.stream().map(a -> a.getPeriode().getTomDato()).max(LocalDate::compareTo).orElse(null);

        if (erKunstig(arbeidsgiver)) {
            if (arbeidsperiodeFom == null) {
                arbeidsperiodeFom = skjæringstidspunkt;
            }
            if (arbeidsperiodeTom == null) {
                arbeidsperiodeTom = TIDENES_ENDE;
            }
        }

        if (arbeidsperiodeFom == null) {
            throw new IllegalStateException("Fant ingen arbeidsperide for skjæringstidspunkt " + skjæringstidspunkt +
                    ", arbeidsgiver " + arbeidsgiver +
                    " og referanse " + iaRef);
        }

        return Intervall.fraOgMedTilOgMed(arbeidsperiodeFom, arbeidsperiodeTom);
    }

    private boolean erKunstig(Arbeidsgiver arbeidsgiver) {
        return arbeidsgiver != null && arbeidsgiver.getErVirksomhet() && OrgNummer.KUNSTIG_ORG.equals(arbeidsgiver.getIdentifikator());
    }

    public static LocalDateTimeline<Boolean> finnAnsettelseTidslinje(Arbeidsgiver arbeidsgiver,
                                                                     InternArbeidsforholdRefDto arbeidsforholdRefDto,
                                                                     InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunktBeregning) {
        YrkesaktivitetFilterDto filter = new YrkesaktivitetFilterDto(iayGrunnlag.getArbeidsforholdInformasjon(), iayGrunnlag.getAktørArbeidFraRegister());
        Collection<YrkesaktivitetDto> yrkesaktiviteterForBeregning = filter.getYrkesaktiviteterForBeregning();
        var yrkesaktiviteter = yrkesaktiviteterForBeregning.stream().filter(ya -> ya.gjelderFor(arbeidsgiver, arbeidsforholdRefDto))
                .toList();
        var ansattTidslinje = finnAnsettelseTidslinje(yrkesaktiviteter);
        var permisjonTidslinje = finnPermisjonstidslinje(arbeidsgiver, arbeidsforholdRefDto, iayGrunnlag, skjæringstidspunktBeregning, yrkesaktiviteterForBeregning);
        return ansattTidslinje.disjoint(permisjonTidslinje);
    }

    private static LocalDateTimeline<Boolean> finnPermisjonstidslinje(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRefDto, InntektArbeidYtelseGrunnlagDto iayGrunnlag, LocalDate skjæringstidspunktBeregning, Collection<YrkesaktivitetDto> yrkesaktiviteterForBeregning) {
        var alleYtelser = iayGrunnlag.getAktørYtelseFraRegister().map(AktørYtelseDto::getAlleYtelser).orElse(Collections.emptyList());
        var permisjonFilter = new PermisjonFilter(alleYtelser, yrkesaktiviteterForBeregning, skjæringstidspunktBeregning);
        return permisjonFilter.tidslinjeForPermisjoner(arbeidsgiver, arbeidsforholdRefDto);
    }


    public static LocalDateTimeline<Boolean> finnAnsettelseTidslinje(Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        var ansattperioder = finnAnsattperioderForYrkesaktiviteter(yrkesaktiviteter);

        var segmenterMedAnsettelse = ansattperioder.stream()
                .map(AktivitetsAvtaleDto::getPeriode)
                .map(p -> new LocalDateTimeline<>(List.of(new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), true))))
                .collect(Collectors.toList());

        var timeline = new LocalDateTimeline<Boolean>(List.of());

        for (LocalDateTimeline<Boolean> localDateSegments : segmenterMedAnsettelse) {
            timeline = timeline.combine(localDateSegments, StandardCombinators::coalesceRightHandSide, LocalDateTimeline.JoinStyle.CROSS_JOIN);
        }

        return timeline.compress();
    }

    private static List<AktivitetsAvtaleDto> finnAnsattperioderForYrkesaktiviteter(Collection<YrkesaktivitetDto> yrkesaktiviteter) {
        return yrkesaktiviteter.stream()
                .flatMap(ya -> ya.getAlleAnsettelsesperioder().stream())
                .collect(Collectors.toList());
    }

}
