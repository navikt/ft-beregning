package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon;

public class Arbeidsgiver {

	private String orgnr;
	private String aktørId;


	private Arbeidsgiver(String orgnr, String aktørId) {
		this.orgnr = orgnr;
		this.aktørId = aktørId;
	}

	public static Arbeidsgiver medOrgnr(String orgnr) {
		return new Arbeidsgiver(orgnr, null);
	}


	public static Arbeidsgiver medAktørId(String aktørId) {
		return new Arbeidsgiver(null, aktørId);
	}

	public String getIdentifikator() {
		if (orgnr != null) {
			return orgnr;
		}
		return aktørId;
	}

	public boolean erOrganisasjon() {
		return orgnr != null;
	}

}
