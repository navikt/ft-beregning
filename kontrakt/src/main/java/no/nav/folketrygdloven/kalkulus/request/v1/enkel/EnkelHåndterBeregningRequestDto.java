package no.nav.folketrygdloven.kalkulus.request.v1.enkel;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;
import no.nav.folketrygdloven.kalkulus.håndtering.v1.HåndterBeregningDto;

/**
 * Spesifikasjon for å løse avklaringsbehov for en behandling.
 */
public record EnkelHåndterBeregningRequestDto(@Valid @NotNull UUID behandlingUuid,
                                              @Valid @NotNull Saksnummer saksnummer,
                                              @Valid @NotNull KalkulatorInputDto kalkulatorInput,
                                              @Valid @NotNull @Size(min = 1) List<HåndterBeregningDto> håndterBeregningDtoList) {}
