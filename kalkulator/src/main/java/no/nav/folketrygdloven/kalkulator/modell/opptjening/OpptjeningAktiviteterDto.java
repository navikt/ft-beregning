package no.nav.folketrygdloven.kalkulator.modell.opptjening;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.MidlertidigInaktivType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.typer.AktørId;

public class OpptjeningAktiviteterDto {

    private MidlertidigInaktivType midlertidigInaktivType;

    private final List<OpptjeningPeriodeDto> opptjeningPerioder = new ArrayList<>();

    public OpptjeningAktiviteterDto(Collection<OpptjeningPeriodeDto> perioder, MidlertidigInaktivType midlertidigInaktivType) {
        this.opptjeningPerioder.addAll(perioder);
        this.midlertidigInaktivType = midlertidigInaktivType;
    }

    public OpptjeningAktiviteterDto(Collection<OpptjeningPeriodeDto> perioder) {
        this.opptjeningPerioder.addAll(perioder);
    }

    public OpptjeningAktiviteterDto(OpptjeningPeriodeDto... perioder) {
        this.opptjeningPerioder.addAll(Arrays.asList(perioder));
    }

    public List<OpptjeningPeriodeDto> getOpptjeningPerioder() {
        return Collections.unmodifiableList(opptjeningPerioder);
    }

    public MidlertidigInaktivType getMidlertidigInaktivType() {
        return midlertidigInaktivType;
    }

    public boolean erMidlertidigInaktiv() {
        return midlertidigInaktivType != null;
    }

    public static class OpptjeningPeriodeDto {

        private OpptjeningAktivitetType type;
        private Intervall periode;
        private String arbeidsgiverOrgNummer;
        private String arbeidsgiverAktørId;

        /**
         * For virksomheter så er internt arbeidsforhold ref generert unikt på bakgrunn av virksomhetens oppgitt eksterne arbeidsforhold ref.
         * <p>
         * For private personer som arbeidsgivere vil ArbeidsforholdRef være linket
         * til ekstern arbeidsforhold ref som er syntetisk skapt (ved UUID#namedUUIDFromBytes). Så er altså ikke noe Altinn sender inn eller som på
         * annet vis fås i inntetksmelding for private arbeidsgiver. Brukes kun til å skille ulike arbeidstyper for samme privat person internt.
         */
        private InternArbeidsforholdRefDto arbeidsforholdId;

        OpptjeningPeriodeDto() {
        }

        private OpptjeningPeriodeDto(OpptjeningAktivitetType type,
                                     Intervall periode,
                                     String arbeidsgiverOrgNummer,
                                     String arbeidsgiverAktørId,
                                     InternArbeidsforholdRefDto arbeidsforholdId) {
            this.type = Objects.requireNonNull(type, "type");
            this.periode = Objects.requireNonNull(periode, "periode");

            // sjekk preconditions
            if (arbeidsgiverAktørId != null) {
                this.arbeidsgiverAktørId = arbeidsgiverAktørId;
                if (arbeidsgiverOrgNummer != null) {
                    throw new IllegalArgumentException("Kan ikke ha orgnummer dersom personlig arbeidsgiver: " + this);
                }
            } else if (arbeidsgiverOrgNummer != null) {
                this.arbeidsgiverOrgNummer = arbeidsgiverOrgNummer;
                this.arbeidsforholdId = arbeidsforholdId;
            } else {
                if (arbeidsforholdId != null) {
                    throw new IllegalArgumentException("Kan ikke ha arbeidsforholdId dersom ikke har arbeidsgiver: " + this);
                }
            }

        }

        public OpptjeningAktivitetType getType() {
            return type;
        }

        public OpptjeningAktivitetType getOpptjeningAktivitetType() {
            return type;
        }

        public Intervall getPeriode() {
            return periode;
        }

        public String getArbeidsgiverOrgNummer() {
            return arbeidsgiverOrgNummer;
        }

        public String getArbeidsgiverAktørId() {
            return arbeidsgiverAktørId;
        }

        public InternArbeidsforholdRefDto getArbeidsforholdId() {
            return arbeidsforholdId == null ?
                    InternArbeidsforholdRefDto.nullRef() : arbeidsforholdId;
        }

        public Optional<Arbeidsgiver> getArbeidsgiver() {
            if (arbeidsgiverAktørId == null && arbeidsgiverOrgNummer == null) {
                return Optional.empty();
            }
            return arbeidsgiverAktørId != null ? Optional.of(Arbeidsgiver.person(new AktørId(arbeidsgiverAktørId))) :
                    Optional.of(Arbeidsgiver.virksomhet(arbeidsgiverOrgNummer));
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, periode, arbeidsgiverOrgNummer, arbeidsgiverAktørId, arbeidsforholdId);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || !obj.getClass().equals(this.getClass())) {
                return false;
            }
            OpptjeningPeriodeDto other = (OpptjeningPeriodeDto) obj;
            return Objects.equals(this.arbeidsgiverOrgNummer, other.arbeidsgiverOrgNummer)
                && Objects.equals(this.arbeidsgiverAktørId, other.arbeidsgiverAktørId)
                && Objects.equals(this.periode, other.periode)
                && Objects.equals(this.type, other.type)
                && Objects.equals(this.arbeidsforholdId, other.arbeidsforholdId);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName()
                + "<type=" + type
                + ", periode=" + periode
                + (arbeidsgiverOrgNummer == null ? "" : ", arbeidsgiverOrgNummer=" + arbeidsgiverOrgNummer)
                + (arbeidsgiverAktørId == null ? "" : ", arbeidsgiverAktørId=" + arbeidsgiverAktørId)
                + (arbeidsforholdId == null ? "" : ", arbeidsforholdId=" + arbeidsforholdId)
                + ">";
        }

    }

    public static OpptjeningPeriodeDto nyPeriode(OpptjeningAktivitetType type,
                                                 Intervall periode,
                                                 String arbeidsgiverOrgNummer,
                                                 String aktørId,
                                                 InternArbeidsforholdRefDto arbeidsforholdId) {
        return new OpptjeningPeriodeDto(type, periode, arbeidsgiverOrgNummer, aktørId, arbeidsforholdId);
    }


    public static OpptjeningPeriodeDto nyPeriodeOrgnr(OpptjeningAktivitetType type,
                                                      Intervall periode,
                                                      String arbeidsgiverOrgNummer) {
        return new OpptjeningPeriodeDto(type, periode, arbeidsgiverOrgNummer, null, null);
    }

    /** Lag ny opptjening periode for angitt aktivitet uten arbeidsgiver (kan ikke vøre type ARBEID). */
    public static OpptjeningPeriodeDto nyPeriode(OpptjeningAktivitetType type, Intervall periode) {
        kanIkkeVæreArbeid(type);
        return new OpptjeningPeriodeDto(type, periode, null, null, null);
    }


    /** Med enkel, registrert arbeidsgiver. ArbeidsforholdReferanse optional. */
    public static OpptjeningAktiviteterDto fraOrgnr(OpptjeningAktivitetType type, Intervall periode, String orgnr, InternArbeidsforholdRefDto arbId) {
        return new OpptjeningAktiviteterDto(nyPeriode(type, periode, orgnr, null, arbId));
    }

    /** Med enkel, registrert arbeidsgiver. Ikke arbeidsforholdReferanse. */
    public static OpptjeningAktiviteterDto fraOrgnr(OpptjeningAktivitetType type, Intervall periode, String orgnr) {
        return new OpptjeningAktiviteterDto(nyPeriode(type, periode, orgnr, null, null));
    }

    /** Med enkel, privat arbeidsgiver. Merk - angi arbeidsgivers aktørId, ikke søkers. */
    public static OpptjeningAktiviteterDto fraAktørId(OpptjeningAktivitetType type, Intervall periode, String arbeidsgiverAktørId) {
        return new OpptjeningAktiviteterDto(nyPeriode(type, periode, null, arbeidsgiverAktørId, null));
    }


    /** Med enkel, aktivitet uten arbeidsgiver (kan ikke være {@link OpptjeningAktivitetType#ARBEID}. */
    public static OpptjeningAktiviteterDto fra(OpptjeningAktivitetType type, Intervall periode) {
        kanIkkeVæreArbeid(type);
        return new OpptjeningAktiviteterDto(nyPeriode(type, periode, null, null, null));
    }

    private static void kanIkkeVæreArbeid(OpptjeningAktivitetType type) {
        if (OpptjeningAktivitetType.ARBEID.equals(type)) {
            throw new IllegalArgumentException("Kan ikke angi Opptjening av type ARBEID uten å angi arbeidsgiver.");
        }
    }

}
