package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum SammenligningsgrunnlagType implements Kodeverdi, DatabaseKode, KontraktKode {

    SAMMENLIGNING_AT,
    SAMMENLIGNING_FL,
    SAMMENLIGNING_AT_FL,
    SAMMENLIGNING_SN,
    SAMMENLIGNING_ATFL_SN,
    SAMMENLIGNING_MIDL_INAKTIV,
    ;

    @Override
    public String getKode() {
        return name();
    }


}
