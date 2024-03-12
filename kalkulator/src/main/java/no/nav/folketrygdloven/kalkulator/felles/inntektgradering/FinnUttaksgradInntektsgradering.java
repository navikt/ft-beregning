package no.nav.folketrygdloven.kalkulator.felles.inntektgradering;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class FinnUttaksgradInntektsgradering {

    private FinnUttaksgradInntektsgradering() {
    }

    public static LocalDateTimeline<BigDecimal> finn(BeregningsgrunnlagInput input) {
        var beregningsgrunnlagRegel = new MapBeregningsgrunnlagFraVLTilRegel().map(input, input.getBeregningsgrunnlag());
        beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().forEach(KalkulusRegler::finnGrenseverdi);
        return beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().stream()
                .filter(p -> p.getTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt() != null)
                .map(p -> new LocalDateSegment<>(p.getPeriodeFom(), p.getPeriodeTom(), p.getTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), LocalDateTimeline::new));

    }

}
