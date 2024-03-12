package no.nav.folketrygdloven.kalkulator.felles.periodesplitting;

import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagPeriodeDto;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public class PeriodeSplitter<V> {


    private final SplittPeriodeConfig<V> config;

    public PeriodeSplitter(SplittPeriodeConfig<V> config) {
        this.config = config;
    }

    /**
     * Splitter opp og periodiserer beregningsgrunnlag på opppgitt periodetidslinje
     * Verdier i tidslinje må vere ulike på hver side av splitten (kjører compress før combine)
     *
     * @param beregningsgrunnlag   Beregningsgrunnlag
     * @param nyePerioderTidslinje Tidslinje for nye perioder
     * @return Beregningsgrunnlag med splittet perioder
     */
    public BeregningsgrunnlagDto splittPerioder(BeregningsgrunnlagDto beregningsgrunnlag,
                                                LocalDateTimeline<V> nyePerioderTidslinje) {

        var eksisterendePerioder = beregningsgrunnlag.getBeregningsgrunnlagPerioder();

        var eksisterendePerioderTidslinje = new LocalDateTimeline<>(eksisterendePerioder.stream()
                .map(p -> new LocalDateSegment<>(new LocalDateInterval(p.getPeriode().getFomDato(), p.getPeriode().getTomDato()), p)).toList());

        var komprimert = nyePerioderTidslinje.compress(config.getAbutsPredikatForCompress(), config.getLikhetsPredikatForCompress(), StandardCombinators::leftOnly);

        var resultatPerioder = eksisterendePerioderTidslinje.combine(komprimert, config.getNyePerioderCombinator(), LocalDateTimeline.JoinStyle.LEFT_JOIN);

        var nyttBg = BeregningsgrunnlagDto.builder(beregningsgrunnlag).fjernAllePerioder().build();

        config.getPeriodeTidslinjeMapper().apply(resultatPerioder, nyePerioderTidslinje)
                .toSegments()
                .forEach(s -> {
                    if (s.getValue() != null) {
                        BeregningsgrunnlagPeriodeDto.oppdater(s.getValue()).build(nyttBg);
                    }
                });

        return nyttBg;
    }


}
