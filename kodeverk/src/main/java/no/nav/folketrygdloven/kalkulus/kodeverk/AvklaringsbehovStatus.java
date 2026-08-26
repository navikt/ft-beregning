package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Mulige statuser for avklaringsbehov.
 * OPPRETTET - Avklaringsbehovet er opprettet og ligger uløst på koblingen
 * UTFØRT - Avklaringsbehovet er opprettet og løst av saksbehandler
 * AVBRUTT - Avklaringsbehovet var før opprettet men er blitt avbrutt
 * SPOLT - Avklaringsbehovet ble opprettet ved spoling og trenger ikke løses
 */
public enum AvklaringsbehovStatus implements Kodeverdi, DatabaseKode, KontraktKode {

    OPPRETTET("OPPR"),
    UTFØRT("UTFO"),
    AVBRUTT("AVBR"),
    SPOLT("SPOL");

    private static final Map<String, AvklaringsbehovStatus> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private final String kode;

    AvklaringsbehovStatus(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }

    public static AvklaringsbehovStatus fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent AvklaringsbehovStatus: " + kode);
        }
        return ad;
    }

}
