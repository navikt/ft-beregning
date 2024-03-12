package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Organisasjonstype implements Kodeverdi, KontraktKode {

    JURIDISK_ENHET,
    VIRKSOMHET,
    KUNSTIG,
    UDEFINERT,
    ;



    @JsonCreator
    public static Organisasjonstype fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return Organisasjonstype.valueOf(kode);
    }


    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
