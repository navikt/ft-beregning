package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum FaktaVurderingKilde implements Kodeverdi, DatabaseKode {

    SAKSBEHANDLER,
    KALKULATOR,
    UDEFINERT,
    ;

    @Override
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
