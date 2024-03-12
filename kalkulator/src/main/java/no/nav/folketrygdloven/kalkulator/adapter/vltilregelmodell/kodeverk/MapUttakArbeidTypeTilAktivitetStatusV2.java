package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk;


import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.kalkulator.modell.svp.AktivitetDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class MapUttakArbeidTypeTilAktivitetStatusV2 {

    public static AktivitetStatusV2 mapAktivitetStatus(AktivitetDto utbetalingsgradAktivitet) {
        UttakArbeidType uttakArbeidType = utbetalingsgradAktivitet.getUttakArbeidType();
        if (UttakArbeidType.ORDINÆRT_ARBEID.equals(uttakArbeidType)) {
            return AktivitetStatusV2.AT;
        }
        if (UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE.equals(uttakArbeidType)) {
            return AktivitetStatusV2.SN;
        }
        if (UttakArbeidType.FRILANS.equals(uttakArbeidType)) {
            return AktivitetStatusV2.FL;
        }
        if (UttakArbeidType.MIDL_INAKTIV.equals(uttakArbeidType)) {
            return AktivitetStatusV2.IN;
        }
        if (UttakArbeidType.DAGPENGER.equals(uttakArbeidType)) {
            return AktivitetStatusV2.DP;
        }
        if (UttakArbeidType.SYKEPENGER_AV_DAGPENGER.equals(uttakArbeidType)) {
            return AktivitetStatusV2.SP_AV_DP;
        }
        if (UttakArbeidType.PLEIEPENGER_AV_DAGPENGER.equals(uttakArbeidType)) {
            return AktivitetStatusV2.PSB_AV_DP;
        }
        if (UttakArbeidType.BRUKERS_ANDEL.equals(uttakArbeidType)) {
            return AktivitetStatusV2.BA;
        }
        if (UttakArbeidType.IKKE_YRKESAKTIV.equals(uttakArbeidType)) {
            return AktivitetStatusV2.AT;
        }
        if (UttakArbeidType.ANNET.equals(uttakArbeidType)) {
            throw new IllegalArgumentException("Kan ikke gradere " + UttakArbeidType.ANNET);
        }
        throw new IllegalArgumentException("Ukjent UttakArbeidType '" + utbetalingsgradAktivitet + "' kan ikke mappe til " + AktivitetStatus.class.getName());
    }


}
