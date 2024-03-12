package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum NaturalYtelseType implements Kodeverdi, KontraktKode {

    ELEKTRISK_KOMMUNIKASJON,
    AKSJER_UNDERKURS, // Aksjer og grunnfondsbevis til underkurs
    LOSJI,
    KOST_DOEGN,
    BESOEKSREISER_HJEM,
    KOSTBESPARELSE_HJEM,
    RENTEFORDEL_LAAN,
    BIL,
    KOST_DAGER,
    BOLIG,
    FORSIKRINGER, // Skattepliktig del av forsikringer
    FRI_TRANSPORT,
    OPSJONER,
    TILSKUDD_BARNEHAGE,
    ANNET,
    BEDRIFTSBARNEHAGE,
    YRKESBIL_KILOMETER,
    YRKESBIL_LISTEPRIS,
    UTENLANDSK_PENSJONSORDNING, // Innbetaling til utenlandsk pensjonsordning
    UDEFINERT,
    ;


    @JsonCreator
    public static NaturalYtelseType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return NaturalYtelseType.valueOf(kode);
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }


}
