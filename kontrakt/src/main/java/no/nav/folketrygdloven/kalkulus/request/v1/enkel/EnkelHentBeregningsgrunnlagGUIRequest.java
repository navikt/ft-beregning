package no.nav.folketrygdloven.kalkulus.request.v1.enkel;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

/**
 * Spesifikasjon for å hente beregningsgrunnlag GUI dto for en behandling.
 */
public record EnkelHentBeregningsgrunnlagGUIRequest(@Valid @NotNull UUID behandlingUuid,
                                                    @Valid @NotNull Saksnummer saksnummer,
                                                    @Valid @NotNull KalkulatorInputDto kalkulatorInput) {}
