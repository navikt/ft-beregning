package no.nav.folketrygdloven.kalkulus.request.v1.enkel;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

/**
 * Spesifikasjon for å hente hvilke steg en kobling eller dens originalkobling har vært innom.
 */
public record EnkelGrunnlagTilstanderRequestDto(@Valid @NotNull Saksnummer saksnummer,
                                                @Valid @NotNull UUID behandlingUuid,
                                                @Valid UUID originalBehandlingUuid) {}
