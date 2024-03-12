package no.nav.folketrygdloven.kalkulus.request.v1;

import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

import java.util.UUID;

public interface KalkulusRequest {

    /** Angitt saksnummer for sporing */
    Saksnummer getSaksnummer();

    /** BehandingUuid */
    UUID getBehandlingUuid();

}
