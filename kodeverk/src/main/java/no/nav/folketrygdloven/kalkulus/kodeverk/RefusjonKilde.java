package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum RefusjonKilde implements Kodeverdi {
    // TODO trenger vi flere kilder?
    INNTEKTSMELDING,
    SAKSBEHANDLER,
    ;

    @Override
    public String getKode() {
        return name();
    }
}
