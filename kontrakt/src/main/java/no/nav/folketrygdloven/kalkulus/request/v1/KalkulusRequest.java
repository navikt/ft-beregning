package no.nav.folketrygdloven.kalkulus.request.v1;

import java.util.UUID;

import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

public interface KalkulusRequest {

    /** Angitt saksnummer for sporing */
    Saksnummer getSaksnummer();

    /** BehandingUuid */
    UUID getBehandlingUuid();

}
