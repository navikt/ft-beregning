package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import static no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR;

import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.PeriodeSplitter;
import no.nav.folketrygdloven.kalkulator.felles.periodesplitting.SplittPeriodeConfig;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

public class ForlengelsePeriodeTjeneste {

    public static BeregningsgrunnlagDto splittVedStartAvForlengelse(BeregningsgrunnlagInput input, BeregningsgrunnlagDto beregningsgrunnlag) {
        if (!input.getForlengelseperioder().isEmpty()) {
            var forlengelseSegmenter = input.getForlengelseperioder().stream()
                    .map(p -> new LocalDateSegment<>(p.getFomDato(), p.getTomDato(), Boolean.TRUE))
                    .toList();
            var forlengelseTidslinje = new LocalDateTimeline<>(forlengelseSegmenter);
            return getPeriodeSplitter().splittPerioder(beregningsgrunnlag, forlengelseTidslinje);
        }
        return beregningsgrunnlag;
    }

    private static PeriodeSplitter<Boolean> getPeriodeSplitter() {
        SplittPeriodeConfig<Boolean> splittPeriodeConfig = SplittPeriodeConfig.defaultConfig(ENDRING_I_AKTIVITETER_SØKT_FOR);
        return new PeriodeSplitter<>(splittPeriodeConfig);
    }


}
