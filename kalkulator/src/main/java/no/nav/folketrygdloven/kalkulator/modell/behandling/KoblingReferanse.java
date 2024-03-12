package no.nav.folketrygdloven.kalkulator.modell.behandling;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import no.nav.folketrygdloven.kalkulus.kodeverk.FagsakYtelseType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;


/**
 * Minimal metadata for en behandling.
 */
public class KoblingReferanse {

    private Long koblingId;

    private FagsakYtelseType fagsakYtelseType;

    /**
     * Søkers aktørid.
     */
    private AktørId aktørId;

    /**
     * Original behandling id (i tilfelle dette f.eks er en revurdering av en annen behandling.
     */
    private Optional<Long> originalKoblingId;

    /**
     * Inneholder relevante tidspunkter for en behandling
     */
    private Skjæringstidspunkt skjæringstidspunkt;

    /** Eksternt refererbar UUID for behandling. */
    private UUID koblingUuid;

    public KoblingReferanse() {
    }

    private KoblingReferanse(FagsakYtelseType fagsakYtelseType, AktørId aktørId, // NOSONAR
                             Long koblingId, UUID koblingUuid, Optional<Long> originalKoblingId, Skjæringstidspunkt skjæringstidspunkt) {
        this.fagsakYtelseType = fagsakYtelseType;
        this.aktørId = aktørId;
        this.koblingId = koblingId;
        this.koblingUuid = koblingUuid;
        this.originalKoblingId = originalKoblingId;
        this.skjæringstidspunkt = skjæringstidspunkt;
    }


    public static KoblingReferanse fra(FagsakYtelseType fagsakYtelseType,
                                       AktørId aktørId, // NOSONAR
                                       Long koblingId,
                                       UUID koblingUuid,
                                       Optional<Long> originalKoblingId,
                                       Skjæringstidspunkt skjæringstidspunkt) {
        return new KoblingReferanse(fagsakYtelseType,
                aktørId,
                koblingId,
            koblingUuid,
            originalKoblingId,
            skjæringstidspunkt);
    }


    public Long getKoblingId() {
        return koblingId;
    }

    public UUID getKoblingUuid() {
        return koblingUuid;
    }

    public Long getId() {
        return getKoblingId();
    }

    public Optional<Long> getOriginalKoblingId() {
        return originalKoblingId;
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return fagsakYtelseType;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public Skjæringstidspunkt getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public LocalDate getSkjæringstidspunktBeregning() {
        // precondition
        return skjæringstidspunkt.getSkjæringstidspunktBeregning();
    }

    public LocalDate getFørsteUttaksdato() {
        // precondition
        return skjæringstidspunkt.getFørsteUttaksdato();
    }

    public LocalDate getSkjæringstidspunktOpptjening() {
        // precondition
        return skjæringstidspunkt.getSkjæringstidspunktOpptjening();
    }

    @Override
    public int hashCode() {
        return Objects.hash(koblingId, originalKoblingId, fagsakYtelseType, aktørId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        KoblingReferanse other = (KoblingReferanse) obj;
        return Objects.equals(koblingId, other.koblingId)
            && Objects.equals(aktørId, other.aktørId)
            && Objects.equals(fagsakYtelseType, other.fagsakYtelseType)
            && Objects.equals(originalKoblingId, other.originalKoblingId);
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + String.format(
            "<behandlingId=%s, fagsakType=%s, aktørId=%s, skjæringstidspunjkt=%s, originalBehandlingId=%s>",
                koblingId, fagsakYtelseType, aktørId, skjæringstidspunkt, originalKoblingId);
    }

    /**
     * Lag immutable copy av referanse med satt utledet skjæringstidspunkt.
     */
    public KoblingReferanse medSkjæringstidspunkt(LocalDate utledetSkjæringstidspunkt) {
        return new KoblingReferanse(getFagsakYtelseType(),
            getAktørId(),
                getId(),
            getKoblingUuid(),
            getOriginalKoblingId(),
            Skjæringstidspunkt.builder()
                .medSkjæringstidspunktBeregning(utledetSkjæringstidspunkt)
                .build());
    }

    /**
     * Lag immutable copy av referanse med mulighet til å legge til skjæringstidspunkt av flere typer
     */
    public KoblingReferanse medSkjæringstidspunkt(Skjæringstidspunkt skjæringstidspunkt) {
        return new KoblingReferanse(getFagsakYtelseType(),
            getAktørId(),
                getId(),
            getKoblingUuid(),
            getOriginalKoblingId(),
            skjæringstidspunkt);
    }

}
