package no.nav.folketrygdloven.kalkulus.request.v1.enkel;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.KalkulatorInputDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningSteg;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

/**
 * Spesifikasjon for å beregne basert på input.
 */
public record EnkelBeregnRequestDto(@Valid @NotNull Saksnummer saksnummer,
                                    @Valid @NotNull UUID behandlingUuid,
                                    @Valid @NotNull PersonIdent aktør,
                                    @Valid @NotNull FagsakYtelseType ytelseSomSkalBeregnes,
                                    @Valid @NotNull BeregningSteg stegType,
                                    @Valid @NotNull KalkulatorInputDto kalkulatorInput,
                                    @Valid UUID originalBehandlingUuid) {}
