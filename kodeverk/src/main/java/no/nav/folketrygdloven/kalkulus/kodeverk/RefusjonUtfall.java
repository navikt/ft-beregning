package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum RefusjonUtfall implements Kodeverdi {

    INNVILGET,
    AVSLÅTT,
    REDUSERT,
    ;

    @Override
    public String getKode() {
        return name();
    }
}
