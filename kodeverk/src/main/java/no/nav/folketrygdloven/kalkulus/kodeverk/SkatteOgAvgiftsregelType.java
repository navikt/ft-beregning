package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum SkatteOgAvgiftsregelType implements Kodeverdi, KontraktKode {

    SÆRSKILT_FRADRAG_FOR_SJØFOLK,
    SVALBARD,
    SKATTEFRI_ORGANISASJON,
    NETTOLØNN_FOR_SJØFOLK,
    NETTOLØNN,
    KILDESKATT_PÅ_PENSJONER,
    JAN_MAYEN_OG_BILANDENE,

    UDEFINERT,
    ;

    @JsonCreator
    public static SkatteOgAvgiftsregelType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return SkatteOgAvgiftsregelType.valueOf(kode);
    }


    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
