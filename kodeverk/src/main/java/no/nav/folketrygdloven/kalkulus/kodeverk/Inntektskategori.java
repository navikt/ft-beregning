package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Inntektskategori implements Kodeverdi, DatabaseKode, KontraktKode {

    ARBEIDSTAKER,
    FRILANSER,
    SELVSTENDIG_NÆRINGSDRIVENDE,
    DAGPENGER,
    ARBEIDSAVKLARINGSPENGER,
    SJØMANN,
    DAGMAMMA,
    JORDBRUKER,
    FISKER,
    ARBEIDSTAKER_UTEN_FERIEPENGER,
    UDEFINERT,
    ;


    @JsonCreator
    public static Inntektskategori fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return Inntektskategori.valueOf(kode);
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
