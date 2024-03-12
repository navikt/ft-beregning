package no.nav.folketrygdloven.kalkulus.kodeverk;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


public enum ArbeidType implements Kodeverdi, KontraktKode {

    ETTERLØNN_SLUTTPAKKE,
    FORENKLET_OPPGJØRSORDNING,
    FRILANSER, // Frilanser, samlet aktivitet
    FRILANSER_OPPDRAGSTAKER, // Frilansere/oppdragstakere/honorar/mm, register
    LØNN_UNDER_UTDANNING,
    MARITIMT_ARBEIDSFORHOLD,
    MILITÆR_ELLER_SIVILTJENESTE,
    ORDINÆRT_ARBEIDSFORHOLD,
    PENSJON_OG_ANDRE_TYPER_YTELSER_UTEN_ANSETTELSESFORHOLD(
    ),
    NÆRING, // Selvstendig næringsdrivende
    UTENLANDSK_ARBEIDSFORHOLD,
    VENTELØNN_VARTPENGER,
    VANLIG,
    UDEFINERT,
    ;

    public static final Set<ArbeidType> AA_REGISTER_TYPER = Set.of(
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
            ArbeidType.MARITIMT_ARBEIDSFORHOLD,
            ArbeidType.FORENKLET_OPPGJØRSORDNING);



    @JsonCreator
    public static ArbeidType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return ArbeidType.valueOf(kode);
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

}
