package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum YtelseKilde implements Kodeverdi, KontraktKode {

    FPSAK, // Ytelse vedtatt i FP-sak (flere ytelser)
    K9SAK, // Ytelse vedtatt i K9-sak (flere ytelser)
    VLSP, // Ytelse vedtatt i ny sykepengeløsning (kun sykepenger)
    INFOTRYGD, // Ytelse vedtatt i Infotrygd (mange ytelser)
	ARENA, // Ytelse vedtatt i Arena (flere ytelser
	KELVIN, // Ytelse vedtatt i Kelvin (kun AAP)
	@JsonEnumDefaultValue UDEFINERT, // Standard verdi - men kan også få null in
    ;

    @Override
    public String getKode() {
        return name();
    }

}
