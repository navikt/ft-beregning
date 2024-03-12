package no.nav.folketrygdloven.kalkulus.kodeverk;

/**
 * Kodeverk som er portet til java.
 */
public interface DatabaseKode extends Kodeverdi {

    default String getDatabaseKode() {
        return getKode();
    }

}
