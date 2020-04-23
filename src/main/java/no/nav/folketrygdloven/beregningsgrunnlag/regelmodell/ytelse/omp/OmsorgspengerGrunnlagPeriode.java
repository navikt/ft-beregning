package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.omp;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;

public class OmsorgspengerGrunnlagPeriode {

    private Periode periode;
    private BigDecimal maksRefusjonForPeriode;

    public OmsorgspengerGrunnlagPeriode(Periode periode,
                                        BigDecimal maksRefusjonForPeriode) {
        this.periode = periode;
        this.maksRefusjonForPeriode = maksRefusjonForPeriode;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getMaksRefusjonForPeriode() {
        return maksRefusjonForPeriode;
    }

}
