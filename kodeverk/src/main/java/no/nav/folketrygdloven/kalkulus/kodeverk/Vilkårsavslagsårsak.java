package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum Vilkårsavslagsårsak implements Kodeverdi, KontraktKode {

    ATFL_SAMME_ORG,
    SØKT_FL_INGEN_FL_INNTEKT,
    FOR_LAVT_BG,
    FOR_LAVT_BG_8_47,
    AVKORTET_GRUNNET_ANNEN_INNTEKT;

    @Override
    public String getKode() {
        return name();
    }



}
