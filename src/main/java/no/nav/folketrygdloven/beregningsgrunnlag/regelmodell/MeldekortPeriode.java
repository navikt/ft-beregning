package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import java.math.BigDecimal;

public class MeldekortPeriode {

    private Periode periode;
    private BigDecimal dagsats;
    private BigDecimal utbetalingsgrad;
    private AktivitetStatusV2 aktivitetStatus;

    public MeldekortPeriode(Periode periode, BigDecimal dagsats, BigDecimal utbetalingsgrad, AktivitetStatusV2 aktivitetStatus) {
        this.periode = periode;
        this.dagsats = dagsats;
        this.utbetalingsgrad = utbetalingsgrad;
        this.aktivitetStatus = aktivitetStatus;
    }

    public AktivitetStatusV2 getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getDagsats() {
        return dagsats;
    }

    public BigDecimal getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public BigDecimal finnUtbetaling() {
        return dagsats.multiply(utbetalingsgrad);
    }


}
