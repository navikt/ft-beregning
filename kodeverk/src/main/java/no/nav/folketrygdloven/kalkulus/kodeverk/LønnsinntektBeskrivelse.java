package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LønnsinntektBeskrivelse implements Kodeverdi, KontraktKode {
    KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE,
    UDEFINERT,
    ;


    @JsonCreator
    public static LønnsinntektBeskrivelse fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return LønnsinntektBeskrivelse.valueOf(kode);
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
