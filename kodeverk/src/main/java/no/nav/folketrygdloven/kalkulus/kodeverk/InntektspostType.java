package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum InntektspostType implements Kodeverdi, KontraktKode {

    UDEFINERT,
    LØNN,
    YTELSE,
    VANLIG,
    SELVSTENDIG_NÆRINGSDRIVENDE,
    NÆRING_FISKE_FANGST_FAMBARNEHAGE,
    ;


    @JsonCreator
    public static InntektspostType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return InntektspostType.valueOf(kode);
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
