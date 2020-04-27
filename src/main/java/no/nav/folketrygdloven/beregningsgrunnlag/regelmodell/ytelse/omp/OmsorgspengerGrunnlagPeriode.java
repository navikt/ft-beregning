package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;

public class OmsorgspengerGrunnlagPeriode {

    private Periode periode;
    private BigDecimal minsteRefusjonForPeriode;

    public OmsorgspengerGrunnlagPeriode(Periode periode,
                                        BigDecimal minRefusjonForPeriode) {
        this.periode = periode;
        this.minsteRefusjonForPeriode = minRefusjonForPeriode;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getMinsteRefusjonForPeriode() {
        return minsteRefusjonForPeriode;
    }

}
