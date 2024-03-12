package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class OppgittOpptjeningDto {

    private UUID uuid;
    private List<OppgittArbeidsforholdDto> oppgittArbeidsforhold;
    private List<OppgittEgenNæringDto> egenNæring;
    private List<OppgittAnnenAktivitetDto> annenAktivitet;
    private OppgittFrilansDto frilans;

    @SuppressWarnings("unused")
    private OppgittOpptjeningDto() {
        // hibernate
    }

    public OppgittOpptjeningDto(UUID eksternReferanse) {
        Objects.requireNonNull(eksternReferanse, "eksternReferanse");
        this.uuid = eksternReferanse;
        // setter tidspunkt til nå slik at dette også er satt for nybakte objekter uten å lagring
    }

    /** Identifisere en immutable instans av grunnlaget unikt og er egnet for utveksling (eks. til abakus eller andre systemer) */
    public UUID getEksternReferanse() {
        return uuid;
    }

    public List<OppgittArbeidsforholdDto> getOppgittArbeidsforhold() {
        if (this.oppgittArbeidsforhold == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(oppgittArbeidsforhold);
    }

    public List<OppgittEgenNæringDto> getEgenNæring() {
        if (this.egenNæring == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(egenNæring);
    }

    public List<OppgittAnnenAktivitetDto> getAnnenAktivitet() {
        if (this.annenAktivitet == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(annenAktivitet);
    }

    public Optional<OppgittFrilansDto> getFrilans() {
        return Optional.ofNullable(frilans);
    }

    void leggTilFrilans(OppgittFrilansDto frilans) {
        if (frilans != null) {
            frilans.setOppgittOpptjening(this);
            this.frilans = frilans;
        } else {
            this.frilans = null;
        }
    }

    void leggTilAnnenAktivitet(OppgittAnnenAktivitetDto annenAktivitet) {
        if (this.annenAktivitet == null) {
            this.annenAktivitet = new ArrayList<>();
        }
        if (annenAktivitet != null) {
            this.annenAktivitet.add(annenAktivitet);
        }
    }

    void leggTilEgenNæring(OppgittEgenNæringDto egenNæring) {
        if (this.egenNæring == null) {
            this.egenNæring = new ArrayList<>();
        }
        if (egenNæring != null) {
            this.egenNæring.add(egenNæring);
        }
    }

    void leggTilOppgittArbeidsforhold(OppgittArbeidsforholdDto oppgittArbeidsforhold) {
        if (this.oppgittArbeidsforhold == null) {
            this.oppgittArbeidsforhold = new ArrayList<>();
        }
        if (oppgittArbeidsforhold != null) {
            this.oppgittArbeidsforhold.add(oppgittArbeidsforhold);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof OppgittOpptjeningDto))
            return false;
        OppgittOpptjeningDto that = (OppgittOpptjeningDto) o;
        return Objects.equals(oppgittArbeidsforhold, that.oppgittArbeidsforhold) &&
            Objects.equals(egenNæring, that.egenNæring) &&
            Objects.equals(annenAktivitet, that.annenAktivitet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oppgittArbeidsforhold, egenNæring, annenAktivitet);
    }

    @Override
    public String toString() {
        return "OppgittOpptjeningEntitet{" +
            "oppgittArbeidsforhold=" + oppgittArbeidsforhold +
            ", egenNæring=" + egenNæring +
            ", annenAktivitet=" + annenAktivitet +
            '}';
    }

    /**
     * Brukes til å filtrere bort tomme oppgitt opptjening elementer ved migrering. Bør ikke være nødvendig til annet.
     *
     * har minst noe av oppgitt arbeidsforhold, egen næring, annen aktivitet eller frilans.
     */
    public boolean harOpptjening() {
        return !getOppgittArbeidsforhold().isEmpty() || !getEgenNæring().isEmpty() || !getAnnenAktivitet().isEmpty() || !getFrilans().isEmpty();
    }
}
