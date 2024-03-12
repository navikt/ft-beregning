package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;

public enum Inntektskategori {
    ARBEIDSTAKER(AktivitetStatus.AT),
    FRILANSER(AktivitetStatus.FL),
    SELVSTENDIG_NÆRINGSDRIVENDE(AktivitetStatus.SN),
    DAGPENGER(AktivitetStatus.DP),
    ARBEIDSAVKLARINGSPENGER(AktivitetStatus.AAP),
    SJØMANN(AktivitetStatus.AT),
    DAGMAMMA(AktivitetStatus.SN),
    JORDBRUKER(AktivitetStatus.SN),
    FISKER(AktivitetStatus.SN),
    ARBEIDSTAKER_UTEN_FERIEPENGER(AktivitetStatus.AT),
    UDEFINERT(AktivitetStatus.UDEFINERT);

    private AktivitetStatus aktivitetStatus;

    Inntektskategori(AktivitetStatus aktivitetStatus) {
        this.aktivitetStatus = aktivitetStatus;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }
}
