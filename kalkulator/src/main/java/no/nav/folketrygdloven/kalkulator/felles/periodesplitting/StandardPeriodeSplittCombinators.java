package no.nav.folketrygdloven.kalkulator.felles.periodesplitting;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator;

public class StandardPeriodeSplittCombinators {


    public static <V> LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, V, BeregningsgrunnlagPeriodeDto> splittPerioderOgSettÅrsakCombinator(PeriodeÅrsak periodeårsak, SkalSettePeriodeårsakSjekker<V> skalSettePeriodeårsak) {
        return (di, lhs, rhs) -> {
            if (lhs != null) {
                var nyPeriodeBuilder = BeregningsgrunnlagPeriodeDto.kopier(lhs.getValue())
                        .medBeregningsgrunnlagPeriode(di.getFomDato(), di.getTomDato());
                if (skalSettePeriodeårsak.sjekk(di, lhs, rhs)) {
                    nyPeriodeBuilder.leggTilPeriodeÅrsak(periodeårsak);
                }
                return new LocalDateSegment<>(di, nyPeriodeBuilder.build());
            }
            return null;
        };
    }

    @FunctionalInterface
    public interface SkalSettePeriodeårsakSjekker<V> {
        boolean sjekk(LocalDateInterval var1, LocalDateSegment<BeregningsgrunnlagPeriodeDto> var2, LocalDateSegment<V> var3);
    }


}
