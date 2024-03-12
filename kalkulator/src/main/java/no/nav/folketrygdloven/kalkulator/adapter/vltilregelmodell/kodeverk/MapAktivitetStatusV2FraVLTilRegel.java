package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk;

import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;

public class MapAktivitetStatusV2FraVLTilRegel {
    private static final Map<AktivitetStatus, AktivitetStatusV2> MAP_AKTIVITETSTATUS =
            Map.of(
                    AktivitetStatus.ARBEIDSTAKER, AktivitetStatusV2.AT,
                    AktivitetStatus.FRILANSER, AktivitetStatusV2.FL,
                    AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE, AktivitetStatusV2.SN,
                    AktivitetStatus.MILITÆR_ELLER_SIVIL, AktivitetStatusV2.MS,
                    AktivitetStatus.DAGPENGER, AktivitetStatusV2.DP,
                    AktivitetStatus.ARBEIDSAVKLARINGSPENGER, AktivitetStatusV2.AAP,
                    AktivitetStatus.SYKEPENGER_AV_DAGPENGER, AktivitetStatusV2.SP_AV_DP,
                    AktivitetStatus.PLEIEPENGER_AV_DAGPENGER, AktivitetStatusV2.PSB_AV_DP
            );

    private static final Map<Inntektskategori, AktivitetStatusV2> MAP_INNTEKTSKATEGORI = Map.of(
            Inntektskategori.ARBEIDSAVKLARINGSPENGER, AktivitetStatusV2.AAP,
            Inntektskategori.ARBEIDSTAKER, AktivitetStatusV2.AT,
            Inntektskategori.SJØMANN, AktivitetStatusV2.AT,
            Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER, AktivitetStatusV2.AT,
            Inntektskategori.DAGPENGER, AktivitetStatusV2.DP,
            Inntektskategori.FRILANSER, AktivitetStatusV2.FL,
            Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, AktivitetStatusV2.SN,
            Inntektskategori.DAGMAMMA, AktivitetStatusV2.SN,
            Inntektskategori.JORDBRUKER, AktivitetStatusV2.SN,
            Inntektskategori.FISKER, AktivitetStatusV2.SN
    );

    private MapAktivitetStatusV2FraVLTilRegel() {
        // skjul public constructor
    }

    public static AktivitetStatusV2 map(AktivitetStatus aktivitetStatus, Inntektskategori inntektskategori) {
        if (aktivitetStatus.equals(AktivitetStatus.BRUKERS_ANDEL)) {
            return MAP_INNTEKTSKATEGORI.get(inntektskategori);
        }
        return MAP_AKTIVITETSTATUS.get(aktivitetStatus);
    }
}
