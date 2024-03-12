package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum FrisinnBehandlingType implements Kodeverdi, KontraktKode {

    REVURDERING,
    NY_SØKNADSPERIODE;



    @Override
    public String getKode() {
        return name();
    }



}
