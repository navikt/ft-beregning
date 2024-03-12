package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;


public enum AktivitetStatus implements Kodeverdi, DatabaseKode, KontraktKode {

    MIDLERTIDIG_INAKTIV("MIDL_INAKTIV", Inntektskategori.UDEFINERT),
    ARBEIDSAVKLARINGSPENGER("AAP", Inntektskategori.ARBEIDSAVKLARINGSPENGER),
    ARBEIDSTAKER("AT", Inntektskategori.ARBEIDSTAKER),
    DAGPENGER("DP", Inntektskategori.DAGPENGER),
    SYKEPENGER_AV_DAGPENGER("SP_AV_DP", Inntektskategori.DAGPENGER),
    PLEIEPENGER_AV_DAGPENGER("PSB_AV_DP", Inntektskategori.DAGPENGER),
    FRILANSER("FL", Inntektskategori.FRILANSER),
    MILITÆR_ELLER_SIVIL("MS", Inntektskategori.ARBEIDSTAKER),
    SELVSTENDIG_NÆRINGSDRIVENDE("SN", Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE),
    KOMBINERT_AT_FL("AT_FL", Inntektskategori.UDEFINERT),
    KOMBINERT_AT_SN("AT_SN", Inntektskategori.UDEFINERT),
    KOMBINERT_FL_SN("FL_SN", Inntektskategori.UDEFINERT),
    KOMBINERT_AT_FL_SN("AT_FL_SN", Inntektskategori.UDEFINERT),
    BRUKERS_ANDEL("BA", Inntektskategori.UDEFINERT),
    KUN_YTELSE("KUN_YTELSE", Inntektskategori.UDEFINERT),

    TTLSTØTENDE_YTELSE("TY", Inntektskategori.UDEFINERT),
    VENTELØNN_VARTPENGER("VENTELØNN_VARTPENGER", Inntektskategori.UDEFINERT),

    UDEFINERT(KodeKonstanter.UDEFINERT, Inntektskategori.UDEFINERT);

    private static final Map<String, AktivitetStatus> KODER = new LinkedHashMap<>();

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
    private final Inntektskategori inntektskategori;

    AktivitetStatus(String kode, Inntektskategori inntektskategori) {
        this.kode = kode;
        this.inntektskategori = inntektskategori;
    }

    public static AktivitetStatus fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent AktivitetStatus: " + kode);
        }
        return ad;
    }


    private static final Set<AktivitetStatus> AT_STATUSER = new HashSet<>(Arrays.asList(ARBEIDSTAKER,
            KOMBINERT_AT_FL_SN, KOMBINERT_AT_SN, KOMBINERT_AT_FL));

    private static final Set<AktivitetStatus> SN_STATUSER = new HashSet<>(Arrays.asList(SELVSTENDIG_NÆRINGSDRIVENDE,
            KOMBINERT_AT_FL_SN, KOMBINERT_AT_SN, KOMBINERT_FL_SN));

    private static final Set<AktivitetStatus> FL_STATUSER = new HashSet<>(Arrays.asList(FRILANSER,
            KOMBINERT_AT_FL_SN, KOMBINERT_AT_FL, KOMBINERT_FL_SN));

    private static final Set<AktivitetStatus> DP_STATUSER = new HashSet<>(Arrays.asList(DAGPENGER, SYKEPENGER_AV_DAGPENGER, PLEIEPENGER_AV_DAGPENGER));

    public boolean erArbeidstaker() {
        return AT_STATUSER.contains(this);
    }

    public boolean erSelvstendigNæringsdrivende() {
        return SN_STATUSER.contains(this);
    }

    public boolean erFrilanser() {
        return FL_STATUSER.contains(this);
    }

    public boolean erDagpenger() {
        return DP_STATUSER.contains(this);
    }

    @Override
    public String getKode() {
        return kode;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }
}
