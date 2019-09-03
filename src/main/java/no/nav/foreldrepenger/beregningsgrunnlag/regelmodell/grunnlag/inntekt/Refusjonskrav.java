package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;

public class Refusjonskrav {

    private Periode periode;
    private BigDecimal månedsbeløp;

    public Refusjonskrav(BigDecimal månedsbeløp, LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(månedsbeløp);
        Objects.requireNonNull(fom);
        this.periode = new Periode(fom, tom);
        this.månedsbeløp = månedsbeløp;
    }

    public LocalDate getFom() {
        return periode.getFom();
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getMånedsbeløp() {
        return månedsbeløp;
    }
}
