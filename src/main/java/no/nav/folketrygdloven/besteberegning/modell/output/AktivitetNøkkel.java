package no.nav.folketrygdloven.besteberegning.modell.output;

import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;

public class AktivitetNøkkel {

	private String orgnr;
	private String aktørId;
	private String arbeidsforholdId;
	private Aktivitet type;

	private AktivitetNøkkel(String orgnr, String aktørId, String arbeidsforholdId, Aktivitet type) {
		this.orgnr = orgnr;
		this.aktørId = aktørId;
		this.arbeidsforholdId = arbeidsforholdId;
		this.type = type;
	}

	public static AktivitetNøkkel forOrganisasjon(String orgnr, String arbeidsforholdId) {
		return new AktivitetNøkkel(orgnr, null, arbeidsforholdId, Aktivitet.ARBEIDSTAKERINNTEKT);
	}


	public static AktivitetNøkkel forPrivatperson(String aktørId, String arbeidsforholdId) {
		return new AktivitetNøkkel(null, aktørId, arbeidsforholdId, Aktivitet.ARBEIDSTAKERINNTEKT);
	}


	public static AktivitetNøkkel forType(Aktivitet type) {
		if (type.equals(Aktivitet.ARBEIDSTAKERINNTEKT)) {
			throw new IllegalStateException("Kan ikke vere arbeidstakerinntekt uten orgnr eller aktørId.");
		}
		return new AktivitetNøkkel(null, null, null, type);
	}

	public String getOrgnr() {
		return orgnr;
	}

	public String getAktørId() {
		return aktørId;
	}

	public String getArbeidsforholdId() {
		return arbeidsforholdId;
	}

	public Aktivitet getType() {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AktivitetNøkkel that = (AktivitetNøkkel) o;
		return Objects.equals(orgnr, that.orgnr) &&
				Objects.equals(aktørId, that.aktørId) &&
				Objects.equals(arbeidsforholdId, that.arbeidsforholdId) &&
				type == that.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(orgnr, aktørId, arbeidsforholdId, type);
	}

	@Override
	public String toString() {
		return "AktivitetNøkkel{" +
				"orgnr='" + orgnr + '\'' +
				", aktørId='" + aktørId + '\'' +
				", arbeidsforholdId='" + arbeidsforholdId + '\'' +
				", type=" + type +
				'}';
	}
}
