package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt;

public enum Inntektskilde {
    INNTEKTSKOMPONENTEN_BEREGNING(InntektPeriodeType.MÅNEDLIG),
    INNTEKTSKOMPONENTEN_SAMMENLIGNING(InntektPeriodeType.MÅNEDLIG),
    INNTEKTSMELDING(InntektPeriodeType.MÅNEDLIG),
    SIGRUN(InntektPeriodeType.ÅRLIG),
    SØKNAD(InntektPeriodeType.ÅRLIG),
    TILSTØTENDE_YTELSE_DP_AAP(InntektPeriodeType.DAGLIG);

    private InntektPeriodeType inntektPeriodeType;

    Inntektskilde(InntektPeriodeType inntektPeriodeType) {
        this.inntektPeriodeType = inntektPeriodeType;
    }

    public InntektPeriodeType getInntektPeriodeType() {
        return inntektPeriodeType;
    }
}
