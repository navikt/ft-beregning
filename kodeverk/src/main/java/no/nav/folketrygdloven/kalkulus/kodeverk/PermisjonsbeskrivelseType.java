package no.nav.folketrygdloven.kalkulus.kodeverk;


import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum PermisjonsbeskrivelseType implements Kodeverdi, KontraktKode {

    UDEFINERT,
    PERMISJON,
    UTDANNINGSPERMISJON,
    UTDANNINGSPERMISJON_IKKE_LOVFESTET,
    UTDANNINGSPERMISJON_LOVFESTET,
    VELFERDSPERMISJON,
    ANNEN_PERMISJON_IKKE_LOVFESTET,
    ANNEN_PERMISJON_LOVFESTET,
    PERMISJON_MED_FORELDREPENGER,
    PERMITTERING,
    PERMISJON_VED_MILITÆRTJENESTE,
    ;


    public static final Set<PermisjonsbeskrivelseType> K9_VELFERDSPERMISJON = Set.of(
            PermisjonsbeskrivelseType.VELFERDSPERMISJON,
            PermisjonsbeskrivelseType.ANNEN_PERMISJON_LOVFESTET
    );



    @JsonCreator
    public static PermisjonsbeskrivelseType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return PermisjonsbeskrivelseType.valueOf(kode);
    }


    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
