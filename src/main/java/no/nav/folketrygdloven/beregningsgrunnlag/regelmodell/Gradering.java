package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Gradering {

    private Periode periode;
    private BigDecimal utbetalingsprosent;

    public Gradering(Periode periode, BigDecimal utbetalingsprosent) {
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
