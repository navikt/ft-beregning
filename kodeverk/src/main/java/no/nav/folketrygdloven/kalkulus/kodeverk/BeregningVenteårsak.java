package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BeregningVenteårsak implements Kodeverdi, KontraktKode {

    UDEFINERT,
    VENT_INNTEKT_RAPPORTERINGSFRIST,
    VENT_PÅ_SISTE_AAP_MELDEKORT, // Venter på siste meldekort for AAP eller dagpenger før første uttaksdag
    INGEN_PERIODE_UTEN_YTELSE, // FRISINN: Sak settes på vent fordi søker har ytelse de 3 siste årene
    INGEN_AKTIVITETER, // FRISINN: Sak settes på vent fordi søker ikke har aktiviteter
    ;

    @JsonCreator
    public static BeregningVenteårsak fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return BeregningVenteårsak.valueOf(kode);
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
