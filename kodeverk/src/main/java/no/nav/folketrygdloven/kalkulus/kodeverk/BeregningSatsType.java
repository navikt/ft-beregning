package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum BeregningSatsType implements Kodeverdi, DatabaseKode {
    ENGANG,
    GRUNNBELØP,
    GSNITT,
    UDEFINERT,
    ;


    @Override
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
