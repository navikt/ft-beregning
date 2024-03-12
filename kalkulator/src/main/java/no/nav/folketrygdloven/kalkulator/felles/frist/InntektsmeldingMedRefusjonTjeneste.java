package no.nav.folketrygdloven.kalkulator.felles.frist;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.felles.FinnYrkesaktiviteterForBeregningTjeneste;
import no.nav.folketrygdloven.kalkulator.modell.behandling.KoblingReferanse;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningAktivitetAggregatDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.InntektArbeidYtelseGrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.KravperioderPrArbeidsforholdDto;
import no.nav.folketrygdloven.kalkulator.modell.iay.YrkesaktivitetDto;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class InntektsmeldingMedRefusjonTjeneste {

    private InntektsmeldingMedRefusjonTjeneste() {
    }

    public static Set<Arbeidsgiver> finnArbeidsgiverSomHarSøktRefusjonForSent(KoblingReferanse koblingReferanse,
                                                                       InntektArbeidYtelseGrunnlagDto iayGrunnlag,
                                                                       BeregningsgrunnlagGrunnlagDto grunnlag,
                                                                       List<KravperioderPrArbeidsforholdDto> kravperioder,
                                                                       FagsakYtelseType ytelseType) {
        Collection<YrkesaktivitetDto> yrkesaktiviteter = FinnYrkesaktiviteterForBeregningTjeneste.finnYrkesaktiviteter(iayGrunnlag, grunnlag, koblingReferanse.getSkjæringstidspunktBeregning());
        LocalDate skjæringstidspunktBeregning = grunnlag.getBeregningsgrunnlagHvisFinnes().map(BeregningsgrunnlagDto::getSkjæringstidspunkt)
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Skal ha beregningsgrunnlag"));
        BeregningAktivitetAggregatDto gjeldendeAktiviteter = grunnlag.getGjeldendeAktiviteter();
        var harSøktForSentMap = ArbeidsgiverRefusjonskravTjeneste.lagFristTidslinjePrArbeidsgiver(
                yrkesaktiviteter,
                kravperioder,
                gjeldendeAktiviteter,
                skjæringstidspunktBeregning,
                Optional.empty(),
                ytelseType);
        return finnArbeidsgivereSomHarSøktForSent(harSøktForSentMap);
    }

    private static Set<Arbeidsgiver> finnArbeidsgivereSomHarSøktForSent(Map<Arbeidsgiver, LocalDateTimeline<KravOgUtfall>> tidslinjeMap) {
        return tidslinjeMap.entrySet().stream()
                .filter(e -> harMinstEnUnderkjentPeriode(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private static boolean harMinstEnUnderkjentPeriode(LocalDateTimeline<KravOgUtfall> utfallTidslinje) {
        return utfallTidslinje.stream().anyMatch(s -> s.getValue().utfall().equals(Utfall.UNDERKJENT));
    }

}
