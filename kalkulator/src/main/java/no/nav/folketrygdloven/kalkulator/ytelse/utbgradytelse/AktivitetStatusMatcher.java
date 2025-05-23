package no.nav.folketrygdloven.kalkulator.ytelse.utbgradytelse;


import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

public class AktivitetStatusMatcher {

    public static boolean matcherStatus(AktivitetStatus status, UttakArbeidType uttakArbeidType) {
        return (UttakArbeidType.IKKE_YRKESAKTIV.equals(uttakArbeidType) && status.erArbeidstaker()) ||
		        (UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE_IKKE_AKTIV.equals(uttakArbeidType) && status.erSelvstendigNæringsdrivende()) ||
		        (UttakArbeidType.FRILANSER_IKKE_AKTIV.equals(uttakArbeidType) && status.erFrilanser()) ||
                matcherStatusUtenIkkeAktiv(status, uttakArbeidType);
    }

	public static boolean matcherStatusUtenIkkeAktiv(AktivitetStatus status, UttakArbeidType uttakArbeidType) {
		return switch (uttakArbeidType) {
			case FRILANSER_IKKE_AKTIV, SELVSTENDIG_NÆRINGSDRIVENDE_IKKE_AKTIV, IKKE_YRKESAKTIV  -> false;
			default -> mapAktivitetStatus(uttakArbeidType).equals(status);
		};
	}

    private static AktivitetStatus mapAktivitetStatus(UttakArbeidType uttakArbeidType) {
        if (UttakArbeidType.ORDINÆRT_ARBEID.equals(uttakArbeidType)) {
            return AktivitetStatus.ARBEIDSTAKER;
        }
        if (UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE.equals(uttakArbeidType)) {
            return AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE;
        }
        if (UttakArbeidType.FRILANS.equals(uttakArbeidType)) {
            return AktivitetStatus.FRILANSER;
        }
        if (UttakArbeidType.MIDL_INAKTIV.equals(uttakArbeidType)) {
            return AktivitetStatus.BRUKERS_ANDEL;
        }
        if (UttakArbeidType.DAGPENGER.equals(uttakArbeidType)) {
            return AktivitetStatus.DAGPENGER;
        }
        if (UttakArbeidType.SYKEPENGER_AV_DAGPENGER.equals(uttakArbeidType)) {
            return AktivitetStatus.SYKEPENGER_AV_DAGPENGER;
        }
        if (UttakArbeidType.PLEIEPENGER_AV_DAGPENGER.equals(uttakArbeidType)) {
            return AktivitetStatus.PLEIEPENGER_AV_DAGPENGER;
        }
        if (UttakArbeidType.BRUKERS_ANDEL.equals(uttakArbeidType)) {
            return AktivitetStatus.BRUKERS_ANDEL;
        }
        if (UttakArbeidType.ANNET.equals(uttakArbeidType)) {
            throw new IllegalArgumentException("Kan ikke gradere " + UttakArbeidType.ANNET);
        }
        throw new IllegalArgumentException("Ukjent UttakArbeidType '" + uttakArbeidType + "' kan ikke mappe til " + AktivitetStatus.class.getName());
    }


}
