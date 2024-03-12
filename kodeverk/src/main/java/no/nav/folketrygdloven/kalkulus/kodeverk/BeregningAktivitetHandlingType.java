package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum BeregningAktivitetHandlingType implements Kodeverdi, DatabaseKode, KontraktKode {

    BENYTT,
    IKKE_BENYTT,
    UDEFINERT,
    ;

    @JsonCreator
    public static BeregningAktivitetHandlingType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return BeregningAktivitetHandlingType.valueOf(kode);
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }


}
