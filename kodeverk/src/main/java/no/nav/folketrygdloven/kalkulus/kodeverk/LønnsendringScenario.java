package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum LønnsendringScenario implements Kodeverdi, KontraktKode {

    MANUELT_BEHANDLET, // Inntekt er manuelt satt i fakta om beregning
    DELVIS_MÅNEDSINNTEKT_SISTE_MND, // Inntekt er beregnet fra siste måned som har delvis ny og gammel inntekt
    FULL_MÅNEDSINNTEKT_EN_MND, // Inntekt er beregnet fra siste måned som kun har ny inntekt.
    FULL_MÅNEDSINNTEKT_TO_MND, // Inntekt er beregnet fra de siste to månedene som begge har ny inntekt

    ;
    @Override
    public String getKode() {
        return name();
    }

}
