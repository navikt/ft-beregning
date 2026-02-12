package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BeregnetPrAarKilde implements Kodeverdi, KontraktKode {

    SAKSBEHANDLER,
    INNTEKTSMELDING,
    A_ORDNING;

    @Override
    @JsonValue
    public String getKode() {
        return name();
    }

}
