package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum Dekningsgrad {
    DEKNINGSGRAD_60(0.6),
    DEKNINGSGRAD_65(0.65),
    DEKNINGSGRAD_70(0.7),
    DEKNINGSGRAD_80(0.8),
    DEKNINGSGRAD_100(1.0);

    private final double verdi;

    Dekningsgrad(double verdi) {
        this.verdi = verdi;
    }

    public double getVerdi() { return verdi; }
}
