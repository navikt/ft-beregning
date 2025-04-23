package no.nav.folketrygdloven.kalkulator.felles.inntektgradering;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.input.UtbetalingsgradGrunnlag;
import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.StatusOgArbeidsgiver;
import no.nav.folketrygdloven.kalkulator.steg.tilkommetInntekt.TilkommetInntektsforholdTjeneste;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

/**
 * Utleder hvilke aktiviteter som vil bli regnet som tilkommet i hvilke perioder uten å faktisk endre grunnlaget.
 */
public class SimulerTilkomneAktiviteterTjeneste {

    private SimulerTilkomneAktiviteterTjeneste() {
        // Skuler default konstruktør
    }

    public static LocalDateTimeline<Set<StatusOgArbeidsgiver>> utledTilkommetAktivitetPerioder(BeregningsgrunnlagInput beregningsgrunnlagInput) {
        /*
         * XXX: Koden her bør trekkes ut i en egen tjeneste for "tilkommet aktivitet".
         */
        if (!(beregningsgrunnlagInput.getYtelsespesifiktGrunnlag() instanceof UtbetalingsgradGrunnlag)) {
            // Om vi ikke har utbetalingsgradsgrunnlag har vi ikke grunnlag for å vurdere tilkommet inntekt
            return new LocalDateTimeline<>(LocalDateInterval.TIDENES_BEGYNNELSE, LocalDateInterval.TIDENES_ENDE, Set.of());
        }
        var tilkommetTidslinje = TilkommetInntektsforholdTjeneste.finnTilkommetInntektsforholdTidslinje(
                beregningsgrunnlagInput.getSkjæringstidspunktForBeregning(),
                beregningsgrunnlagInput.getBeregningsgrunnlag().getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList(),
                beregningsgrunnlagInput.getYtelsespesifiktGrunnlag(),
                beregningsgrunnlagInput.getIayGrunnlag(),
                true
        );
        return tilkommetTidslinje.filterValue(v -> !v.isEmpty()).compress();
    }

    public static boolean erTilkommetAktivitetIPeriode(LocalDateTimeline<Set<StatusOgArbeidsgiver>> tilkommetAktivitetTidslinje, LocalDateSegment periode, AktivitetStatus aktivitetStatus, Optional<Arbeidsgiver> arbeidsgiver) {
        return tilkommetAktivitetTidslinje.stream()
                .filter(tids -> tids.overlapper(periode))
                .anyMatch(tids -> tids.getValue().stream().anyMatch(akt -> Objects.equals(akt.arbeidsgiver(), arbeidsgiver.orElse(null))
                        && akt.aktivitetStatus().equals(aktivitetStatus)));
    }

}
