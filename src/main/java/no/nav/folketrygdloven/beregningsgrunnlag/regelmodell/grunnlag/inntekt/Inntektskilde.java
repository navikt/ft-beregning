package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt;

public enum Inntektskilde {
    INNTEKTSKOMPONENTEN_BEREGNING(InntektPeriodeType.MÅNEDLIG),
    INNTEKTSKOMPONENTEN_SAMMENLIGNING(InntektPeriodeType.MÅNEDLIG),
    INNTEKTSMELDING(InntektPeriodeType.MÅNEDLIG),
    SIGRUN(InntektPeriodeType.ÅRLIG),
    SØKNAD(InntektPeriodeType.ÅRLIG),
	TILSTØTENDE_YTELSE_DP_AAP(InntektPeriodeType.DAGLIG), // Meldekort
    ANNEN_YTELSE(InntektPeriodeType.DAGLIG),
	YTELSE_VEDTAK(InntektPeriodeType.DAGLIG); // Ytelsevedtak i infotrygd, fp-sak, k9-sak



	private InntektPeriodeType inntektPeriodeType;

    Inntektskilde(InntektPeriodeType inntektPeriodeType) {
        this.inntektPeriodeType = inntektPeriodeType;
    }

    public InntektPeriodeType getInntektPeriodeType() {
        return inntektPeriodeType;
    }
}
