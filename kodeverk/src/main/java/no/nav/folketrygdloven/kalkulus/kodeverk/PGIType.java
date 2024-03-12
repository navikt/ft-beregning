package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum PGIType implements Kodeverdi, KontraktKode {

    LØNN, // Pensjonsgivende inntekt gjennom lønn
    NÆRING, // Pensjonsgivende inntekt gjennom næring
    UDEFINERT
    ;

    @JsonCreator
    public static PGIType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return PGIType.valueOf(kode);
    }


    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
