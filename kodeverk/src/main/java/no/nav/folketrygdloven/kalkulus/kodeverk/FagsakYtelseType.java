package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

/*
 * Ytelser som kalkulus kan beregne etter Ftl kap 8, 9, 14
 */
public enum FagsakYtelseType implements Kodeverdi, DatabaseKode, KontraktKode {

    /**
     * Folketrygdloven K9 ytelser.
     */
    PLEIEPENGER_SYKT_BARN(KodeKonstanter.YT_PLEIEPENGER_SYKT_BARN, YtelseType.PLEIEPENGER_SYKT_BARN),
    PLEIEPENGER_NÆRSTÅENDE(KodeKonstanter.YT_PLEIEPENGER_NÆRSTÅENDE, YtelseType.PLEIEPENGER_NÆRSTÅENDE),
    OMSORGSPENGER(KodeKonstanter.YT_OMSORGSPENGER, YtelseType.OMSORGSPENGER),
    OPPLÆRINGSPENGER(KodeKonstanter.YT_OPPLÆRINGSPENGER, YtelseType.OPPLÆRINGSPENGER),

    /**
     * Folketrygdloven K14 ytelser.
     * - Engangsstønad beregnes ikke i kalkulus
     */
    FORELDREPENGER(KodeKonstanter.YT_FORELDREPENGER, YtelseType.FORELDREPENGER),
    SVANGERSKAPSPENGER(KodeKonstanter.YT_SVANGERSKAPSPENGER, YtelseType.SVANGERSKAPSPENGER),

    /**
     * Ny ytelse for kompenasasjon for koronatiltak for Selvstendig næringsdrivende og Frilansere (Anmodning 10).
     */
    FRISINN(KodeKonstanter.YT_FRISINN, YtelseType.FRISINN),

    UDEFINERT(KodeKonstanter.UDEFINERT, YtelseType.UDEFINERT),
    ;

    private static final Map<String, FagsakYtelseType> KODER = new LinkedHashMap<>();

    public static final Set<FagsakYtelseType> K9_YTELSER = Set.of(
            OMSORGSPENGER,
            PLEIEPENGER_SYKT_BARN,
            PLEIEPENGER_NÆRSTÅENDE,
            OPPLÆRINGSPENGER);

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }


    @JsonValue
    private final String kode;
    @JsonIgnore
    private final YtelseType tilsvarende;

    FagsakYtelseType(String kode, YtelseType tilsvarende) {
        this.kode = kode;
        this.tilsvarende = tilsvarende;
    }

    public static FagsakYtelseType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent FagsakYtelseType: " + kode);
        }
        return ad;
    }

    @Override
    public String getKode() {
        return kode;
    }

    public YtelseType getTilsvarendeYtelseType() {
        return tilsvarende;
    }

}
