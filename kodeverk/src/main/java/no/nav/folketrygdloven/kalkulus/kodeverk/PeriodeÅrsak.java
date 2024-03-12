package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum PeriodeÅrsak implements Kodeverdi, DatabaseKode, KontraktKode {

    NATURALYTELSE_BORTFALT,
    ARBEIDSFORHOLD_AVSLUTTET,
    NATURALYTELSE_TILKOMMER,
    ENDRING_I_REFUSJONSKRAV,
    REFUSJON_OPPHØRER,
    GRADERING,
    GRADERING_OPPHØRER,
    ENDRING_I_AKTIVITETER_SØKT_FOR,
    TILKOMMET_INNTEKT,
    TILKOMMET_INNTEKT_MANUELT,
    TILKOMMET_INNTEKT_AVSLUTTET,
    REFUSJON_AVSLÅTT,
    REPRESENTERER_STORTINGET,
    REPRESENTERER_STORTINGET_AVSLUTTET,
    UDEFINERT,
    ;

    @JsonCreator
    public static PeriodeÅrsak fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return PeriodeÅrsak.valueOf(kode);
    }


    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }


}
