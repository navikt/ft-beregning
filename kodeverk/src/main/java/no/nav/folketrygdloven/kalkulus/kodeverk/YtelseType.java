package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonValue;

/*
 * Alle ytelser som kalkulus forholder seg til i prosessen og som kan være innhentet i IAY-grunnlag
 */
public enum YtelseType implements Kodeverdi, KontraktKode {

    /**
     * Folketrygdloven K4 ytelser.
     */
    DAGPENGER("DAG"),

    /**
     * Ny ytelse for kompenasasjon for koronatiltak for Selvstendig næringsdrivende og Frilansere (Anmodning 10).
     */
    FRISINN(KodeKonstanter.YT_FRISINN),

    /**
     * Folketrygdloven K8 ytelser.
     */
    SYKEPENGER("SP"),

    /**
     * Folketrygdloven K9 ytelser.
     */
    PLEIEPENGER_SYKT_BARN(KodeKonstanter.YT_PLEIEPENGER_SYKT_BARN),
    PLEIEPENGER_NÆRSTÅENDE(KodeKonstanter.YT_PLEIEPENGER_NÆRSTÅENDE),
    OMSORGSPENGER(KodeKonstanter.YT_OMSORGSPENGER),
    OPPLÆRINGSPENGER(KodeKonstanter.YT_OPPLÆRINGSPENGER),

    /**
     * Folketrygdloven K11 ytelser.
     */
    ARBEIDSAVKLARINGSPENGER("AAP"),

    /**
     * Folketrygdloven K14 ytelser.
     */
    ENGANGSTØNAD("ES"),
    FORELDREPENGER(KodeKonstanter.YT_FORELDREPENGER),
    SVANGERSKAPSPENGER(KodeKonstanter.YT_SVANGERSKAPSPENGER),

    /**
     * Folketrygdloven K15 ytelser.
     */
    ENSLIG_FORSØRGER("EF"),

    UDEFINERT(KodeKonstanter.UDEFINERT),
    ;

    private static final Set<YtelseType> ARENA_YTELSER = new HashSet<>(Arrays.asList(DAGPENGER,
            ARBEIDSAVKLARINGSPENGER));

    public static final Set<YtelseType> K9_YTELSER = Set.of(
            OMSORGSPENGER,
            PLEIEPENGER_SYKT_BARN,
            PLEIEPENGER_NÆRSTÅENDE,
            OPPLÆRINGSPENGER);


    @JsonValue
    private final String kode;

    YtelseType(String kode) {
        this.kode = kode;
    }

    public boolean erArenaytelse() {
        return ARENA_YTELSER.contains(this);
    }

    @Override
    public String getKode() {
        return kode;
    }


}
