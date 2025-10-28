package no.nav.folketrygdloven.kalkulator.felles.frist;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.FinnYrkesaktiviteterForBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class InntektsmeldingMedRefusjonTjeneste {

    private InntektsmeldingMedRefusjonTjeneste() {
        // Skjuler default
    }

    public static Set<Arbeidsgiver> finnArbeidsgivereSomHarSøktRefusjonForSent(InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                               BeregningsgrunnlagGrunnlagDto beregningsgrunnlagGrunnlag,
                                                                               List<KravperioderPrArbeidsforholdDto> kravperioder,
                                                                               FagsakYtelseType ytelseType) {
        var skjæringstidspunktBeregning = beregningsgrunnlagGrunnlag.getBeregningsgrunnlagHvisFinnes()
                .map(BeregningsgrunnlagDto::getSkjæringstidspunkt)
                .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Skal ha beregningsgrunnlag"));
        var yrkesaktiviteter = FinnYrkesaktiviteterForBeregningTjeneste.finnYrkesaktiviteter(iayGrunnlag, beregningsgrunnlagGrunnlag, skjæringstidspunktBeregning);

        var harSøktForSentMap = ArbeidsgiverRefusjonskravTjeneste.lagFristTidslinjePrArbeidsgiver(yrkesaktiviteter, kravperioder,
                beregningsgrunnlagGrunnlag.getGjeldendeAktiviteter(), skjæringstidspunktBeregning, Optional.empty(), ytelseType);
        return finnArbeidsgivereMedUnderkjentPeriode(harSøktForSentMap);
    }

    private static Set<Arbeidsgiver> finnArbeidsgivereMedUnderkjentPeriode(Map<Arbeidsgiver, LocalDateTimeline<KravOgUtfall>> tidslinjeMap) {
        return tidslinjeMap.entrySet().stream()
                .filter(e -> harMinstEnUnderkjentPeriode(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private static boolean harMinstEnUnderkjentPeriode(LocalDateTimeline<KravOgUtfall> utfallTidslinje) {
        return utfallTidslinje.stream().anyMatch(s -> s.getValue().utfall().equals(Utfall.UNDERKJENT));
    }
}
