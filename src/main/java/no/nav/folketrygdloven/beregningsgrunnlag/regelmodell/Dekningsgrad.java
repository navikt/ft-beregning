package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import java.math.BigDecimal;

public enum Dekningsgrad {
    DEKNINGSGRAD_65(new BigDecimal("0.65")),
    DEKNINGSGRAD_80(new BigDecimal("0.8")),
    DEKNINGSGRAD_100(BigDecimal.ONE);

    private BigDecimal verdi;

    private Dekningsgrad(BigDecimal verdi) {
        this.verdi = verdi;
    }

    public BigDecimal getVerdi() {
        return verdi;
    }

    public static Dekningsgrad fra(int dekningsgrad) {
        BigDecimal norm = BigDecimal.valueOf(dekningsgrad).divide(BigDecimal.valueOf(100));
        for (var dek : values()) {
            if (dek.getVerdi().compareTo(norm) == 0) {
                return dek;
            }
        }
        throw new UnsupportedOperationException("Støtter ikke dekningsgrad: " + dekningsgrad);
    }

}
