package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum Arbeidskategori implements Kodeverdi, KontraktKode {

    FISKER, // Selvstendig næringsdrivende - Fisker
    ARBEIDSTAKER,
    SELVSTENDIG_NÆRINGSDRIVENDE,
    KOMBINASJON_ARBEIDSTAKER_OG_SELVSTENDIG_NÆRINGSDRIVENDE,
    SJØMANN, // Arbeidstaker - sjømann
    JORDBRUKER, // Selvstendig næringsdrivende - Jordbruker
    DAGPENGER, // Tilstøtende ytelse - dagpenger
    INAKTIV,
    KOMBINASJON_ARBEIDSTAKER_OG_JORDBRUKER, //"Kombinasjon arbeidstaker og selvstendig næringsdrivende - jordbruker
    KOMBINASJON_ARBEIDSTAKER_OG_FISKER, // Kombinasjon arbeidstaker og selvstendig næringsdrivende - fisker
    FRILANSER,
    KOMBINASJON_ARBEIDSTAKER_OG_FRILANSER,
    KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER,
    DAGMAMMA, // Selvstendig næringsdrivende - Dagmamma
    UGYLDIG,
    UDEFINERT,
    ;



    @JsonCreator
    public static Arbeidskategori fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return Arbeidskategori.valueOf(kode);
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }


}
