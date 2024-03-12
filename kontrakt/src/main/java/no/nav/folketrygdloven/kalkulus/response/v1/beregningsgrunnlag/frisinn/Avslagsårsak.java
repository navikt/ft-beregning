package no.nav.folketrygdloven.kalkulus.response.v1.beregningsgrunnlag.frisinn;

public enum Avslagsårsak {

    FOR_LAVT_BG,
    INGEN_FRILANS_I_PERIODE_UTEN_YTELSE,
    AVKORTET_GRUNNET_LØPENDE_INNTEKT,
    AVKORTET_GRUNNET_ANNEN_INNTEKT;

    @Deprecated(forRemoval = true)
    public String getKode() {
        return name();
    }
}
