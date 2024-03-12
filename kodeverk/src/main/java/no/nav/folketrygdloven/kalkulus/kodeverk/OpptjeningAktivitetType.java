package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * <h3>Internt kodeverk</h3>
 * Definerer aktiviteter benyttet til å vurdere Opptjening.
 * <p>
 * Kodeverket sammenstiller data fra {@link ArbeidType}.<br>
 * Senere benyttes dette i mapping til bla. Beregningsgrunnlag.
 */

public enum OpptjeningAktivitetType implements Kodeverdi, DatabaseKode, KontraktKode {

    AAP, // Arbeidsavklaringspenger
    ARBEID,
    DAGPENGER,
    FORELDREPENGER,
    FRILANS,
    MILITÆR_ELLER_SIVILTJENESTE,
    NÆRING,
    OMSORGSPENGER,
    OPPLÆRINGSPENGER,
    PLEIEPENGER,
    FRISINN,
    ETTERLØNN_SLUTTPAKKE,
    SVANGERSKAPSPENGER,
    SYKEPENGER,
    SYKEPENGER_AV_DAGPENGER,
    PLEIEPENGER_AV_DAGPENGER,
    VENTELØNN_VARTPENGER,
    VIDERE_ETTERUTDANNING,
    UTENLANDSK_ARBEIDSFORHOLD,

    UTDANNINGSPERMISJON,
    UDEFINERT,
    ;


    @JsonCreator
    public static OpptjeningAktivitetType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return OpptjeningAktivitetType.valueOf(kode);
    }


    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
