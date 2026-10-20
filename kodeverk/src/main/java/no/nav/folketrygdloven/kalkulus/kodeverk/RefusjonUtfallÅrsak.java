package no.nav.folketrygdloven.kalkulus.kodeverk;

public enum RefusjonUtfallÅrsak implements Kodeverdi {
    // TODO fyll inn med flere årsaker
    IKKE_INNENFOR_FRIST,
    UTMELDT,
    ALLEREDE_DIREKTE_UTBETALT,
    ;

    @Override
    public String getKode() {
        return name();
    }
}
