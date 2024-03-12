package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;

public class OpptjeningsnøkkelDto {

    private InternArbeidsforholdRefDto arbeidsforholdId;
    private String orgNummer;
    private String aktørId;

    public OpptjeningsnøkkelDto(YrkesaktivitetDto yrkesaktivitet) {
        this(yrkesaktivitet.getArbeidsforholdRef(), yrkesaktivitet.getArbeidsgiver());
    }

    public OpptjeningsnøkkelDto(Arbeidsgiver arbeidsgiver) {
        this(null, arbeidsgiver);
    }

    public OpptjeningsnøkkelDto(InternArbeidsforholdRefDto arbeidsforholdId, Arbeidsgiver arbeidsgiver) {
        this(arbeidsforholdId,
            arbeidsgiver.getErVirksomhet() ? arbeidsgiver.getIdentifikator() : null,
            arbeidsgiver.getErVirksomhet() ? null : arbeidsgiver.getIdentifikator());
    }

    public OpptjeningsnøkkelDto(InternArbeidsforholdRefDto arbeidsforholdId, String orgNummer, String aktørId) {
        if (arbeidsforholdId == null && orgNummer == null && aktørId == null) {
            throw new IllegalArgumentException("Minst en av arbeidsforholdId, orgnummer og aktørId må vere ulik null");
        }
        this.arbeidsforholdId = arbeidsforholdId;
        this.orgNummer = orgNummer;
        this.aktørId = aktørId;
    }

    public static OpptjeningsnøkkelDto forOrgnummer(String orgNummer) {
        return new OpptjeningsnøkkelDto(null, orgNummer, null);
    }

    public static OpptjeningsnøkkelDto forArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        return new OpptjeningsnøkkelDto(arbeidsgiver);
    }

    public static OpptjeningsnøkkelDto forArbeidsforholdIdMedArbeidgiver(InternArbeidsforholdRefDto arbeidsforholdId, Arbeidsgiver arbeidsgiver) {
        return new OpptjeningsnøkkelDto(arbeidsforholdId, arbeidsgiver);
    }

    public static OpptjeningsnøkkelDto forType(String id, Type nøkkelType) {
        return nøkkelType.nyNøkkel(id);
    }

    /**
     * Gir en opptjeningsnøkkel basert på følgende rank
     * 1) ArbeidsforholdId er en unik id fra AAreg
     * 2) Org nummer er iden til en virksomhet som fungere som arbeidsgiver
     * 3) Aktør id er iden til en person som fungere som arbeidsgiver
     */
    public String getVerdi() {
        if (harArbeidsforholdId())
            return this.arbeidsforholdId.getReferanse();
        else if (this.orgNummer != null) {
            return this.orgNummer;
        } else if (this.aktørId != null) {
            return this.aktørId;
        } else {
            throw new IllegalStateException("Har ikke nøkkel");
        }
    }

    private boolean harArbeidsforholdId() {
        return this.arbeidsforholdId != null && this.arbeidsforholdId.getReferanse() != null;
    }

    public Optional<InternArbeidsforholdRefDto> getArbeidsforholdRef() {
        return Optional.ofNullable(arbeidsforholdId);
    }

    public Type getType() {
        if (harArbeidsforholdId())
            return Type.ARBEIDSFORHOLD_ID;
        else if (this.orgNummer != null) {
            return Type.ORG_NUMMER;
        } else if (this.aktørId != null) {
            return Type.AKTØR_ID;
        } else {
            return null;
        }
    }


    public boolean matcher(OpptjeningsnøkkelDto other) {
        if (other == null) {
            return false;
        }
        if ((this.getType() != Type.ARBEIDSFORHOLD_ID && other.getType() == this.getType())) {
            return other.getVerdi().equals(this.getVerdi());
        } else if ((this.getType() == Type.ARBEIDSFORHOLD_ID && other.getType() == this.getType())) {
            return matchArbeidsforholdId(other);
        } else {
            return matchOrgEllerAktørId(other);
        }
    }

    private boolean matchOrgEllerAktørId(OpptjeningsnøkkelDto other) {
        if ((Type.ORG_NUMMER.equals(other.getType()) || Type.ORG_NUMMER.equals(this.getType()))
            && (!other.harArbeidsforholdId() || !this.harArbeidsforholdId())) {
            return other.orgNummer != null && other.orgNummer.equals(this.orgNummer);
        }
        return Objects.equals(other.aktørId, this.aktørId);
    }

    private boolean matchArbeidsforholdId(OpptjeningsnøkkelDto other) {
        boolean likArbeidsforholdsId = other.getVerdi().equals(this.getVerdi());
        boolean likArbeidsgiver;
        if ((other.orgNummer != null) || this.orgNummer != null) {
            likArbeidsgiver = other.orgNummer != null && other.orgNummer.equals(this.orgNummer);
        } else {
            likArbeidsgiver = other.aktørId != null && other.aktørId.equals(this.aktørId);
        }
        return likArbeidsforholdsId && likArbeidsgiver;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj.getClass().equals(this.getClass()))) {
            return false;
        }
        OpptjeningsnøkkelDto other = (OpptjeningsnøkkelDto) obj;
        return Objects.equals(aktørId, other.aktørId)
            && Objects.equals(orgNummer, other.orgNummer)
            && Objects.equals(arbeidsforholdId, other.arbeidsforholdId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsforholdId, orgNummer, aktørId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<"
            + "type=" + getType()
            + ", key=" + getVerdi()
            + ">";
    }

    public enum Type {
        ARBEIDSFORHOLD_ID {
            @Override
            OpptjeningsnøkkelDto nyNøkkel(String id) {
                return new OpptjeningsnøkkelDto(InternArbeidsforholdRefDto.ref(id), null, null);
            }
        },
        ORG_NUMMER {
            @Override
            OpptjeningsnøkkelDto nyNøkkel(String id) {
                return new OpptjeningsnøkkelDto(null, id, null);
            }
        },
        AKTØR_ID {
            @Override
            OpptjeningsnøkkelDto nyNøkkel(String id) {
                return new OpptjeningsnøkkelDto(null, null, id);
            }
        };
        abstract OpptjeningsnøkkelDto nyNøkkel(String id);
    }
}
