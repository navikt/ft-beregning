package no.nav.folketrygdloven.kalkulator.felles.frist;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulus.kodeverk.Utfall;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateSegmentCombinator;
import no.nav.fpsak.tidsserie.LocalDateTimeline;

class KombinerRefusjonskravFristTidslinje {

    static LocalDateTimeline<KravOgUtfall> kombinerOgKompress(LocalDateTimeline<KravOgUtfall> t1, LocalDateTimeline<KravOgUtfall> t2) {
        return t1.combine(t2, godkjentHvisEnGodkjentMedKrav(), LocalDateTimeline.JoinStyle.CROSS_JOIN).compress();
    }

    private static LocalDateSegmentCombinator<KravOgUtfall, KravOgUtfall, KravOgUtfall> godkjentHvisEnGodkjentMedKrav() {
        return (dateInterval, lhs, rhs) -> {
            if (lhs != null && rhs != null) {
                return new LocalDateSegment<>(dateInterval, mapTilUtfall(lhs, rhs));
            } else {
                return lhs == null && rhs == null ? null :
                        new LocalDateSegment<>(dateInterval, Objects.requireNonNullElse(lhs, rhs).getValue());
            }
        };
    }

    private static KravOgUtfall mapTilUtfall(LocalDateSegment<KravOgUtfall> lhs, LocalDateSegment<KravOgUtfall> rhs) {
        if (rhs.getValue().utfall().equals(Utfall.GODKJENT)) {
            return rhs.getValue();
        }
        if (lhs.getValue().utfall().equals(Utfall.GODKJENT) && lhs.getValue().refusjonskrav().compareTo(Beløp.ZERO) > 0) {
            return new KravOgUtfall(rhs.getValue().refusjonskrav(), Utfall.GODKJENT);
        }
        return rhs.getValue();
    }

}
