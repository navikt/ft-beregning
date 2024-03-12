package no.nav.folketrygdloven.kalkulator;

public class KalkulatorException extends RuntimeException {

    private String kode;
    private String melding;

    public KalkulatorException(String kode, String melding) {
        super("[" + kode + "] " + melding);
        this.kode = kode;
        this.melding = melding;
    }

    public String getKode() {
        return kode;
    }

    public String getMelding() {
        return melding;
    }

    @Override
    public String toString() {
        return "KalkulatorException{" +
                "kode='" + kode + '\'' +
                ", melding='" + melding + '\'' +
                '}';
    }
}
