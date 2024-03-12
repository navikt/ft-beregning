package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk;

import java.util.EnumMap;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class MapOpptjeningAktivitetFraRegelTilVL {
    private static final EnumMap<Aktivitet, OpptjeningAktivitetType> aktivitetMap =
        new EnumMap<>(Aktivitet.class);

    static {
        aktivitetMap.put(Aktivitet.AAP_MOTTAKER, OpptjeningAktivitetType.AAP);
        aktivitetMap.put(Aktivitet.ARBEIDSTAKERINNTEKT, OpptjeningAktivitetType.ARBEID);
        aktivitetMap.put(Aktivitet.DAGPENGEMOTTAKER, OpptjeningAktivitetType.DAGPENGER);
        aktivitetMap.put(Aktivitet.ETTERLØNN_SLUTTPAKKE, OpptjeningAktivitetType.ETTERLØNN_SLUTTPAKKE);
        aktivitetMap.put(Aktivitet.FORELDREPENGER_MOTTAKER, OpptjeningAktivitetType.FORELDREPENGER);
        aktivitetMap.put(Aktivitet.FRILANSINNTEKT, OpptjeningAktivitetType.FRILANS);
        aktivitetMap.put(Aktivitet.FRISINN_MOTTAKER, OpptjeningAktivitetType.FRISINN);
        aktivitetMap.put(Aktivitet.MILITÆR_ELLER_SIVILTJENESTE, OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE);
        aktivitetMap.put(Aktivitet.NÆRINGSINNTEKT, OpptjeningAktivitetType.NÆRING);
        aktivitetMap.put(Aktivitet.OMSORGSPENGER, OpptjeningAktivitetType.OMSORGSPENGER);
        aktivitetMap.put(Aktivitet.OPPLÆRINGSPENGER, OpptjeningAktivitetType.OPPLÆRINGSPENGER);
        aktivitetMap.put(Aktivitet.PLEIEPENGER_MOTTAKER, OpptjeningAktivitetType.PLEIEPENGER);
        aktivitetMap.put(Aktivitet.SVANGERSKAPSPENGER_MOTTAKER, OpptjeningAktivitetType.SVANGERSKAPSPENGER);
        aktivitetMap.put(Aktivitet.SYKEPENGER_MOTTAKER, OpptjeningAktivitetType.SYKEPENGER);
        aktivitetMap.put(Aktivitet.SYKEPENGER_AV_DAGPENGER_MOTTAKER, OpptjeningAktivitetType.SYKEPENGER_AV_DAGPENGER);
        aktivitetMap.put(Aktivitet.PLEIEPENGER_AV_DAGPENGER_MOTTAKER, OpptjeningAktivitetType.PLEIEPENGER_AV_DAGPENGER);
        aktivitetMap.put(Aktivitet.VENTELØNN_VARTPENGER, OpptjeningAktivitetType.VENTELØNN_VARTPENGER);
        aktivitetMap.put(Aktivitet.VIDERE_ETTERUTDANNING, OpptjeningAktivitetType.VIDERE_ETTERUTDANNING);
    }


    private static final EnumMap<AktivitetStatus, OpptjeningAktivitetType> aktivitetStatusMap =
        new EnumMap<>(AktivitetStatus.class);

    static {
        aktivitetStatusMap.put(AktivitetStatus.SN, OpptjeningAktivitetType.NÆRING);
    }


    private MapOpptjeningAktivitetFraRegelTilVL() {
        // skjul public constructor
    }

    public static OpptjeningAktivitetType map(Aktivitet aktivitet) {
        if (!aktivitetMap.containsKey(aktivitet)) {
            throw new IllegalArgumentException("Utviklerfeil: Mangler mapping for enum Aktivitet " + aktivitet);
        }
        return aktivitetMap.get(aktivitet);
    }

    public static OpptjeningAktivitetType map(AktivitetStatus aktivitetStatus) {
        if (!aktivitetStatusMap.containsKey(aktivitetStatus)) {
            return OpptjeningAktivitetType.UDEFINERT;
        }
        return aktivitetStatusMap.get(aktivitetStatus);
    }
}
