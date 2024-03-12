package no.nav.folketrygdloven.kalkulus.kodeverk;


import com.fasterxml.jackson.annotation.JsonValue;

public enum MidlertidigInaktivType implements Kodeverdi, KontraktKode {

    A("8-47 A"), B("8-47 B");

    @JsonValue
    private final String kode;


    MidlertidigInaktivType(String s) {
        kode = s;
    }

    @Override
    public String getKode() {
        return kode;
    }


}
