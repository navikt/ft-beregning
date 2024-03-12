package no.nav.folketrygdloven.kalkulator.tid;

import java.time.DayOfWeek;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator;

/**
 * Komprimerer tidslinje over helg. Fjerner helger som ikke er henger sammen med andre segmenter og er ulik.
 *
 * @param <V> Tidlinjeparameter
 */
public class TimelineWeekendCompressor<V> implements Consumer<LocalDateSegment<V>> {

    private final NavigableSet<LocalDateSegment<V>> segmenter = new TreeSet<>();
    private final BiPredicate<V, V> equals;
    private final LocalDateSegmentCombinator<V, V, V> combinator;

    TimelineWeekendCompressor(BiPredicate<V, V> e, LocalDateSegmentCombinator<V, V, V> c) {
        this.equals = e;
        this.combinator = c;
    }

    public void accept(LocalDateSegment<V> t) {
        if (segmenter.isEmpty()) {
            segmenter.add(t);
        } else {
            var descIter = segmenter.descendingIterator();
            LocalDateSegment<V> last = descIter.next();
            LocalDateSegment<V> elementBeforeLast;

            if (canBeCompressed(t, last)) {
                // bytt ut og ekspander intervall for siste
                exchangeAndExpand(t, last);
            } else if (isOnlyWeekend(last) && descIter.hasNext() && canBeCompressed(t, elementBeforeLast = descIter.next())) {
                // bytt ut de to siste og ekspander intervall
                var lastExpanded = exchangeAndExpand(last, elementBeforeLast);
                exchangeAndExpand(t, lastExpanded);
            } else {
                segmenter.add(t);
            }
        }
    }

    private boolean canBeCompressed(LocalDateSegment<V> t, LocalDateSegment<V> last) {
        return erKantIKant(t, last) && equals.test(last.getValue(), t.getValue());
    }

    private LocalDateSegment<V> exchangeAndExpand(LocalDateSegment<V> t, LocalDateSegment<V> elementInSegments) {
        segmenter.remove(elementInSegments);
        segmenter.remove(t);
        LocalDateInterval expandedInterval = utvid(t, elementInSegments);
        var expandedSegment = new LocalDateSegment<V>(expandedInterval, combinator.combine(expandedInterval, elementInSegments, t).getValue());
        segmenter.add(expandedSegment);
        return expandedSegment;
    }

    private LocalDateInterval utvid(LocalDateSegment<V> t, LocalDateSegment<V> last) {
        if (last.getLocalDateInterval().abuts(t.getLocalDateInterval())) {
            return last.getLocalDateInterval().expand(t.getLocalDateInterval());
        } else if ((last.getTom().getDayOfWeek().equals(DayOfWeek.FRIDAY) && t.getFom().equals(last.getTom().plusDays(3)))) {
            return last.getLocalDateInterval().expand(new LocalDateInterval(last.getTom().plusDays(1), last.getTom().plusDays(2)))
                    .expand(t.getLocalDateInterval());
        }
        throw new IllegalArgumentException("Intervals do not abut each other or weekend.");
    }

    private boolean isOnlyWeekend(LocalDateSegment<V> t) {
        return t.getLocalDateInterval().days() == 2 && t.getFom().getDayOfWeek().equals(DayOfWeek.SATURDAY) && t.getTom().getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }

    private boolean erKantIKant(LocalDateSegment<V> t, LocalDateSegment<V> last) {
        return last.getLocalDateInterval().abuts(t.getLocalDateInterval()) ||
                (last.getTom().getDayOfWeek().equals(DayOfWeek.FRIDAY) && t.getFom().equals(last.getTom().plusDays(3)));
    }

    public NavigableSet<LocalDateSegment<V>> getSegmenter() {
        return segmenter;
    }

    public void combine(@SuppressWarnings("unused") TimelineWeekendCompressor<V> other) {
        throw new UnsupportedOperationException("Ikke implementert, men påkrevd av Stream#collect for parallell collect"); //$NON-NLS-1$
    }


    public static class CompressorFactory<V> {
        private final BiPredicate<V, V> equals;
        private final LocalDateSegmentCombinator<V, V, V> combinator;

        public CompressorFactory(BiPredicate<V, V> e, LocalDateSegmentCombinator<V, V, V> c) {
            this.equals = e;
            this.combinator = c;
        }

        public TimelineWeekendCompressor<V> get() {
            return new TimelineWeekendCompressor<>(equals, combinator);
        }
    }

}
