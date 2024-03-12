package no.nav.folketrygdloven.kalkulator.felles.periodesplitting;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

/**
 * Definerer konfigurasjon for splitting av perioder. Se PeriodeSpitter
 *
 * @param <V> Type som det splittes over. Alle perioder der likhetsPredikatForCompress vurderes til å ikke komprimere splittes i to nye perioder.
 */
public class SplittPeriodeConfig<V> {

    private BiPredicate<V, V> likhetsPredikatForCompress = StandardPeriodeCompressLikhetspredikat::komprimerNårLike;

    private BiPredicate<LocalDateInterval, LocalDateInterval> abutsPredikatForCompress = LocalDateInterval::abuts;

    /**
     * Combinator for left-joint mot eksisterende perioder
     */
    private final LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, V, BeregningsgrunnlagPeriodeDto> nyePerioderCombinator;

    /**
     * Mapper for post-proseessering av splittet grunnlag (Kan brukes til å sette avsluttet periodeårsak for segmenter som tilstøter nye perioder)
     */
    private BiFunction<LocalDateTimeline<BeregningsgrunnlagPeriodeDto>, LocalDateTimeline<V>, LocalDateTimeline<BeregningsgrunnlagPeriodeDto>> periodeTidslinjeMapper = (t1, t2) -> t1;


    public SplittPeriodeConfig(LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, V, BeregningsgrunnlagPeriodeDto> nyePerioderCombinator) {
        this.nyePerioderCombinator = nyePerioderCombinator;
    }

    public SplittPeriodeConfig(LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, V, BeregningsgrunnlagPeriodeDto> nyePerioderCombinator,
                               BiFunction<LocalDateTimeline<BeregningsgrunnlagPeriodeDto>, LocalDateTimeline<V>, LocalDateTimeline<BeregningsgrunnlagPeriodeDto>> periodeTidslinjeMapper) {
        this.nyePerioderCombinator = nyePerioderCombinator;
        this.periodeTidslinjeMapper = periodeTidslinjeMapper;
    }


    public static <V> SplittPeriodeConfig<V> defaultConfig(PeriodeÅrsak nyPeriodeÅrsak) {
        return new SplittPeriodeConfig<>(
                StandardPeriodeSplittCombinators.splittPerioderOgSettÅrsakCombinator(nyPeriodeÅrsak, (di, lhs, rhs) -> rhs != null)
        );
    }

    public static <V> SplittPeriodeConfig<V> medAvsluttetPeriodeårsakConfig(PeriodeÅrsak nyPeriodeÅrsak, PeriodeÅrsak avsluttetPeriodeårsak, List<Intervall> forlengelseperioder) {
        return new SplittPeriodeConfig<>(
                StandardPeriodeSplittCombinators.splittPerioderOgSettÅrsakCombinator(nyPeriodeÅrsak, (di, lhs, rhs) -> rhs != null),
                StandardPeriodeSplittMappers.settAvsluttetPeriodeårsak(forlengelseperioder, avsluttetPeriodeårsak)
        );
    }

    public BiPredicate<V, V> getLikhetsPredikatForCompress() {
        return likhetsPredikatForCompress;
    }

    public void setLikhetsPredikatForCompress(BiPredicate<V, V> likhetsPredikatForCompress) {
        this.likhetsPredikatForCompress = likhetsPredikatForCompress;
    }

    public LocalDateSegmentCombinator<BeregningsgrunnlagPeriodeDto, V, BeregningsgrunnlagPeriodeDto> getNyePerioderCombinator() {
        return nyePerioderCombinator;
    }

    public BiFunction<LocalDateTimeline<BeregningsgrunnlagPeriodeDto>, LocalDateTimeline<V>, LocalDateTimeline<BeregningsgrunnlagPeriodeDto>> getPeriodeTidslinjeMapper() {
        return periodeTidslinjeMapper;
    }

    public BiPredicate<LocalDateInterval, LocalDateInterval> getAbutsPredikatForCompress() {
        return abutsPredikatForCompress;
    }

    public void setAbutsPredikatForCompress(BiPredicate<LocalDateInterval, LocalDateInterval> abutsPredikatForCompress) {
        this.abutsPredikatForCompress = abutsPredikatForCompress;
    }
}
