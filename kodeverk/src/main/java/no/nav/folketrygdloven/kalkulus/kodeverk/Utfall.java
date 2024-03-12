package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum Utfall implements Kodeverdi, DatabaseKode {

    GODKJENT,
    UNDERKJENT,
    UDEFINERT,
    ;

    @Override
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }


}
