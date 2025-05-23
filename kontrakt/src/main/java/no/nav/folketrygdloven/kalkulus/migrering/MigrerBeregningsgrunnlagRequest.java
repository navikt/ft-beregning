package no.nav.folketrygdloven.kalkulus.migrering;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.folketrygdloven.kalkulus.felles.v1.PersonIdent;
import no.nav.folketrygdloven.kalkulus.felles.v1.Saksnummer;
import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;

public record MigrerBeregningsgrunnlagRequest(@Valid @NotNull Saksnummer saksnummer,
                                              @Valid @NotNull UUID behandlingUuid,
                                              @Valid @NotNull PersonIdent aktør,
                                              @Valid @NotNull FagsakYtelseType ytelseSomSkalBeregnes,
                                              @Valid UUID originalBehandlingUuid,
                                              @Valid @NotNull BeregningsgrunnlagGrunnlagMigreringDto grunnlag,
                                              @Valid @NotNull Boolean erAktivt) {}
