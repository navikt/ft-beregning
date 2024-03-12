package no.nav.folketrygdloven.kalkulus.kodeverk;


public interface BasisKodeverdi {
    String getKode();

    default String getIndexKey() {
        return getKode();
    }
}
