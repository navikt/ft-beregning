package no.nav.folketrygdloven.kalkulus.kodeverk;

/**
 * Mulige statuser for grunnbeløpet på en kobling.
 * NØDVENDIG - Grunnbeløpet som er brukt på koblingen avviker fra nylig utledet grunnbeløp og det kan påvirke beregningen, betyr typisk at gregulering er nødvendig
 * IKKE_NØDVENDIG - Grunnbeløpet som er brukt på koblingen avviker ikke fra nylig utledet grunnbeløp eller nytt grunnbeløp påvirker ikke beregningen, gregulering er ikke nødvendig
 * IKKE_VURDERT - Beregningen har ikke hensyntatt grunnbeløp på koblingen enda
 */
public enum GrunnbeløpReguleringStatus implements Kodeverdi, KontraktKode {

    NØDVENDIG,
    IKKE_NØDVENDIG,
    IKKE_VURDERT;


    @Override
    public String getKode() {
        return name();
    }

}
