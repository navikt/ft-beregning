package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum VirksomhetType implements Kodeverdi, KontraktKode {

    DAGMAMMA,
    FISKE,
    FRILANSER,
    JORDBRUK_SKOGBRUK,
    ANNEN,
    UDEFINERT,
    ;
    @JsonCreator
    public static VirksomhetType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return VirksomhetType.valueOf(kode);
    }


    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
