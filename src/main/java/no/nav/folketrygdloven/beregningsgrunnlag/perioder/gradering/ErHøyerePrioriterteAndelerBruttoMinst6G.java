package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.GraderingPrAktivitet;

class ErHøyerePrioriterteAndelerBruttoMinst6G {

    private static final int ANTALL_G = 6;

    private ErHøyerePrioriterteAndelerBruttoMinst6G() {
        // skjuler default
    }

    static boolean vurder(BigDecimal grunnbeløp,
                          PeriodisertBruttoBeregningsgrunnlag periodisertBruttoBeregningsgrunnlag,
                          GraderingPrAktivitet gradering) {
        int avkortingPrioritet = gradering.getAktivitetStatus().getAvkortingPrioritet();
        BigDecimal prioritertBg = periodisertBruttoBeregningsgrunnlag.getBruttoBeregningsgrunnlag().stream()
            .filter(a -> a.getAktivitetStatus().getAvkortingPrioritet() < avkortingPrioritet)
            .map(BruttoBeregningsgrunnlag::getBruttoPrÅr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal seksG = grunnbeløp.multiply(BigDecimal.valueOf(ANTALL_G));
        return prioritertBg.compareTo(seksG) >= 0;
    }
}
