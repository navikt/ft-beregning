package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum InntektAktivitetType implements Kodeverdi, KontraktKode {

    ARBEIDSTAKERINNTEKT,
    FRILANSINNTEKT,
    YTELSEINNTEKT,
    UDEFINERT;

    @JsonCreator
    public static InntektAktivitetType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return InntektAktivitetType.valueOf(kode);
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
