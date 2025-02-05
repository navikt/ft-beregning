package no.nav.folketrygdloven.kalkulus.migrering;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.fpkalkulus.kontrakt.FpkalkulusYtelser;
import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;

public record MigrerBeregningsgrunnlagRequest(@Valid @NotNull Saksnummer saksnummer,
                                              @Valid @NotNull UUID behandlingUuid,
                                              @Valid @NotNull PersonIdent aktør,
                                              @Valid @NotNull FpkalkulusYtelser ytelseSomSkalBeregnes,
                                              @Valid UUID originalBehandlingUuid,
                                              @Valid @NotNull BeregningsgrunnlagGrunnlagMigreringDto grunnlag) {}
