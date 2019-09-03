package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell;

import java.math.BigDecimal;

public enum Dekningsgrad {
    DEKNINGSGRAD_65(new BigDecimal("0.65")),
    DEKNINGSGRAD_80(new BigDecimal("0.8")),
    DEKNINGSGRAD_100(BigDecimal.ONE);

    private BigDecimal verdi;

    Dekningsgrad(BigDecimal verdi) {
        this.verdi = verdi;
    }

    public BigDecimal getVerdi() {
        return verdi;
    }
}
