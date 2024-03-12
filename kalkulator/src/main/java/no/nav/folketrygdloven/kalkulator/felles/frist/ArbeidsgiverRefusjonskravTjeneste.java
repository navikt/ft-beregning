package no.nav.folketrygdloven.kalkulator.felles.frist;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningRefusjonOverstyringerDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.PerioderForKravDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class ArbeidsgiverRefusjonskravTjeneste {

    private ArbeidsgiverRefusjonskravTjeneste() {
    }

    public static Map<Arbeidsgiver, LocalDateTimeline<KravOgUtfall>> lagFristTidslinjePrArbeidsgiver(Collection<YrkesaktivitetDto> yrkesaktiviteter,
                                                                                                     List<KravperioderPrArbeidsforholdDto> kravperioder,
                                                                                                     BeregningAktivitetAggregatDto gjeldendeAktiviteter,
                                                                                                     LocalDate skjæringstidspunktBeregning,
                                                                                                     Optional<BeregningRefusjonOverstyringerDto> refusjonOverstyringer,
                                                                                                     FagsakYtelseType ytelseType) {
        Map<YrkesaktivitetDto, List<PerioderForKravDto>> yrkesaktivitetKravperioderMap = lagMap(yrkesaktiviteter, kravperioder);
        var tidslinjeMap = new HashMap<Arbeidsgiver, LocalDateTimeline<KravOgUtfall>>();
        for (var entry : yrkesaktivitetKravperioderMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                var yrkesaktivitet = entry.getKey();
                var eksisterendeTidslinje = tidslinjeMap.get(yrkesaktivitet.getArbeidsgiver());
                Optional<LocalDate> overstyrtRefusjonFom = refusjonOverstyringer.stream().flatMap(o -> o.getRefusjonOverstyringer().stream())
                        .filter(o -> o.getArbeidsgiver().equals(yrkesaktivitet.getArbeidsgiver()))
                        .findFirst()
                        .flatMap(BeregningRefusjonOverstyringDto::getFørsteMuligeRefusjonFom);

                var tidslinje = KravTjeneste.lagTidslinjeForYrkesaktivitet(
                        entry.getValue(),
                        yrkesaktivitet,
                        gjeldendeAktiviteter,
                        skjæringstidspunktBeregning,
                        overstyrtRefusjonFom, ytelseType);
                tidslinjeMap.put(yrkesaktivitet.getArbeidsgiver(),
                        eksisterendeTidslinje == null ? tidslinje : KombinerRefusjonskravFristTidslinje.kombinerOgKompress(eksisterendeTidslinje, tidslinje));

            }
        }
        return tidslinjeMap;
    }

    private static Map<YrkesaktivitetDto, List<PerioderForKravDto>> lagMap(Collection<YrkesaktivitetDto> yrkesaktiviteterForBeregning,
                                                                           List<KravperioderPrArbeidsforholdDto> kravlistePrArbeidsgiver) {
        return yrkesaktiviteterForBeregning.stream()
                .collect(Collectors.toMap(y -> y, finnKravForAktivitet(kravlistePrArbeidsgiver)));
    }

    private static Function<YrkesaktivitetDto, List<PerioderForKravDto>> finnKravForAktivitet(List<KravperioderPrArbeidsforholdDto> kravlistePrArbeidsgiver) {
        return y -> kravlistePrArbeidsgiver.stream()
                .filter(kravliste -> y.getArbeidsgiver().equals(kravliste.getArbeidsgiver()) && y.getArbeidsforholdRef().gjelderFor(kravliste.getArbeidsforholdRef()))
                .findFirst()
                .map(KravperioderPrArbeidsforholdDto::finnOverlappMedSisteKrav)
                .orElse(Collections.emptyList());
    }



}
