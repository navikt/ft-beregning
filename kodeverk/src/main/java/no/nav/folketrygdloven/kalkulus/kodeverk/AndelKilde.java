package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum AndelKilde implements Kodeverdi, DatabaseKode, KontraktKode {

    SAKSBEHANDLER_KOFAKBER, // Saksbehandler i steg kontroller fakta beregning
    PROSESS_BESTEBEREGNING, // Prosess for besteberegning
    SAKSBEHANDLER_FORDELING, // Saksbehandler i steg for fordeling
    PROSESS_PERIODISERING, // Prosess for periodisering grunnet refusjon/gradering/utbetalingsgrad
    PROSESS_OMFORDELING, // Prosess for automatisk omfordeling
    PROSESS_START, // Start av beregning
    PROSESS_PERIODISERING_TILKOMMET_INNTEKT // Periodisering for tilkommet inntekt
    ;

    @Override
    public String getKode() {
        return name();
    }

}
