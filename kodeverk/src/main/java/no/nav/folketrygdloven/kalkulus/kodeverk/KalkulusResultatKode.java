package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum KalkulusResultatKode implements Kodeverdi, KontraktKode {

    BEREGNET, // Beregning fullført uten avklaringsbehov
    BEREGNET_MED_AVKLARINGSBEHOV // Beregning fullført med avklaringsbehov
    ;

    @Override
    public String getKode() {
        return name();
    }

}
