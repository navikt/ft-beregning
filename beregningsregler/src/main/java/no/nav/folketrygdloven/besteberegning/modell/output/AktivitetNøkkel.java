package no.nav.folketrygdloven.besteberegning.modell.output;

import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.besteberegning.modell.input.YtelseAktivitetType;

public class AktivitetNøkkel {

	private final String orgnr;
	private final String aktørId;
	private final String arbeidsforholdId;
	private final Aktivitet type;
	private final YtelseAktivitetType ytelseGrunnlagType;

	private AktivitetNøkkel(String orgnr, String aktørId, String arbeidsforholdId, Aktivitet type, YtelseAktivitetType ytelseGrunnlagType) {
		this.orgnr = orgnr;
		this.aktørId = aktørId;
		this.arbeidsforholdId = arbeidsforholdId;
		this.type = type;
		this.ytelseGrunnlagType = ytelseGrunnlagType;
	}

	public static AktivitetNøkkel forOrganisasjon(String orgnr, String arbeidsforholdId) {
		return new AktivitetNøkkel(orgnr, null, arbeidsforholdId, Aktivitet.ARBEIDSTAKERINNTEKT, null);
	}

	public static AktivitetNøkkel forPrivatperson(String aktørId, String arbeidsforholdId) {
		return new AktivitetNøkkel(null, aktørId, arbeidsforholdId, Aktivitet.ARBEIDSTAKERINNTEKT, null);
	}

	public static AktivitetNøkkel forType(Aktivitet type) {
		if (type.equals(Aktivitet.ARBEIDSTAKERINNTEKT)) {
			throw new IllegalStateException("Kan ikke vere arbeidstakerinntekt uten orgnr eller aktørId.");
		}
		if (type.erYtelseFraSammenligningsfilter()) {
			throw new IllegalStateException("Trenger både gyldig aktivitet ytelse og grunnlag for ytelsen for å kunne legge den til som aktivitet. Mottok akivitet " + type);
		}
		return new AktivitetNøkkel(null, null, null, type, null);
	}

	public static AktivitetNøkkel forYtelseFraSammenligningsfilter(Aktivitet ytelsetype, YtelseAktivitetType ytelseGrunnlagType) {
		if (!ytelsetype.erYtelseFraSammenligningsfilter() || ytelseGrunnlagType == null) {
			throw new IllegalStateException("Trenger både gyldig aktivitet ytelse og grunnlag for ytelsen for å kunne " +
					"legge den til som aktivitet. Mottok akivitet " + ytelsetype + " og grunnlagtype " + ytelseGrunnlagType);
		}
		return new AktivitetNøkkel(null, null, null, ytelsetype, ytelseGrunnlagType);
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

	public YtelseAktivitetType getYtelseGrunnlagType() {
		return ytelseGrunnlagType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AktivitetNøkkel that = (AktivitetNøkkel) o;
		return Objects.equals(orgnr, that.orgnr) &&
				Objects.equals(aktørId, that.aktørId) &&
				Objects.equals(ytelseGrunnlagType, that.ytelseGrunnlagType) &&
				Objects.equals(arbeidsforholdId, that.arbeidsforholdId) &&
				type == that.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(orgnr, aktørId, arbeidsforholdId, type, ytelseGrunnlagType);
	}

	@Override
	public String toString() {
		return "AktivitetNøkkel{" +
				"orgnr='" + orgnr + '\'' +
				", aktørId='" + aktørId + '\'' +
				", arbeidsforholdId='" + arbeidsforholdId + '\'' +
				", ytelseGrunnlagType='" + ytelseGrunnlagType + '\'' +
				", type=" + type +
				'}';
	}
}
