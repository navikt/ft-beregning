package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum InntektskildeType implements Kodeverdi, KontraktKode {

    UDEFINERT,
    INNTEKT_OPPTJENING,
    INNTEKT_BEREGNING,
    INNTEKT_SAMMENLIGNING,
    SIGRUN,
    VANLIG,
    ;


    @JsonCreator
    public static InntektskildeType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return InntektskildeType.valueOf(kode);
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
