package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;

public class Utbetalingsgrad {

    private Periode periode;
    private BigDecimal utbetalingsprosent;

    public Utbetalingsgrad(Periode periode, BigDecimal utbetalingsprosent) {
        this.periode = periode;
        this.utbetalingsprosent = utbetalingsprosent;
    }

    public Periode getPeriode() {
        return periode;
    }

    public LocalDate getFom() {
        return periode.getFom();
    }

    public LocalDate getTom() {
        return periode.getTom();
    }

    public BigDecimal getUtbetalingsprosent() {
        return utbetalingsprosent;
    }

    @Override
    public String toString() {
        return "Gradering{" +
            "periode=" + periode +
            ", utbetalingsprosent=" + utbetalingsprosent +
            '}';
    }
}
