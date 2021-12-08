package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering;

import java.time.LocalDate;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;

public class Gradering {

    private final Periode periode;

    public Gradering(Periode periode) {
        this.periode = periode;
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

    @Override
    public String toString() {
        return "Gradering{" +
            "periode=" + periode +
            '}';
    }
}
