package no.nav.folketrygdloven.kalkulus.kodeverk;

/**
 * <h3>Internt kodeverk</h3>
 * Definerer typer av handlinger en saksbehandler kan gjøre vedrørende et arbeidsforhold
 * <p>
 */

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ArbeidsforholdHandlingType implements Kodeverdi, KontraktKode {

    UDEFINERT,
    BRUK,
    NYTT_ARBEIDSFORHOLD, // Arbeidsforholdet er ansett som nytt
    BRUK_UTEN_INNTEKTSMELDING, // Bruk, men ikke benytt inntektsmelding
    IKKE_BRUK,
    SLÅTT_SAMMEN_MED_ANNET, // Arbeidsforholdet er slått sammen med et annet
    LAGT_TIL_AV_SAKSBEHANDLER, // Arbeidsforhold lagt til av saksbehandler
    BASERT_PÅ_INNTEKTSMELDING, // Arbeidsforholdet som ikke ligger i AA-reg er basert på inntektsmelding
    BRUK_MED_OVERSTYRT_PERIODE, // Bruk arbeidsforholdet med overstyrt periode
    INNTEKT_IKKE_MED_I_BG, // Inntekten til arbeidsforholdet skal ikke være med i beregningsgrunnlaget
    ;

    private static final Set<ArbeidsforholdHandlingType> MED_OVERSTYRT_PERIODE = Set.of(BRUK_MED_OVERSTYRT_PERIODE, BASERT_PÅ_INNTEKTSMELDING, LAGT_TIL_AV_SAKSBEHANDLER);


    @JsonCreator
    public static ArbeidsforholdHandlingType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return ArbeidsforholdHandlingType.valueOf(kode);
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }

    public boolean erPeriodeOverstyrt() {
        return MED_OVERSTYRT_PERIODE.contains(this);
    }

}
