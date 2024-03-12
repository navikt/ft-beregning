package no.nav.folketrygdloven.kalkulus.kodeverk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Et tilfelle som kan oppstå i fakta om beregning. Hvert tilfelle beskriver en spesifikk situasjon der informasjon må innhentes eller manuell vurdering
 * må gjøres av saksbehandler.
 */
public enum FaktaOmBeregningTilfelle implements Kodeverdi, DatabaseKode, KontraktKode {

    VURDER_TIDSBEGRENSET_ARBEIDSFORHOLD,
    VURDER_SN_NY_I_ARBEIDSLIVET, // Vurder om søker er SN og ny i arbeidslivet
    VURDER_NYOPPSTARTET_FL, // Vurder nyoppstartet frilans
    FASTSETT_MAANEDSINNTEKT_FL, // Fastsett månedsinntekt frilans
    FASTSETT_BG_ARBEIDSTAKER_UTEN_INNTEKTSMELDING, // Fastsette beregningsgrunnlag for arbeidstaker uten inntektsmelding
    VURDER_LØNNSENDRING,
    FASTSETT_MÅNEDSLØNN_ARBEIDSTAKER_UTEN_INNTEKTSMELDING,
    VURDER_AT_OG_FL_I_SAMME_ORGANISASJON, // Vurder om bruker er arbeidstaker og frilanser i samme organisasjon
    FASTSETT_BESTEBEREGNING_FØDENDE_KVINNE,
    VURDER_ETTERLØNN_SLUTTPAKKE, // Vurder om søker har etterlønn og/eller sluttpakke
    FASTSETT_ETTERLØNN_SLUTTPAKKE, // Fastsett søkers beregningsgrunnlag for etterlønn og/eller sluttpakke andel
    VURDER_MOTTAR_YTELSE, // Vurder om søker mottar ytelse for aktivitet
    VURDER_BESTEBEREGNING, // Vurder om søker skal ha besteberegning
    VURDER_MILITÆR_SIVILTJENESTE, // Vurder om søker har hatt militær- eller siviltjeneste i opptjeningsperioden
    VURDER_REFUSJONSKRAV_SOM_HAR_KOMMET_FOR_SENT, // Vurder refusjonskrav fremsatt for sent skal være med i beregning
    FASTSETT_BG_KUN_YTELSE, // Fastsett beregningsgrunnlag for kun ytelse uten arbeidsforhold
    TILSTØTENDE_YTELSE, // Avklar beregningsgrunnlag og inntektskategori for tilstøtende ytelse
    FASTSETT_ENDRET_BEREGNINGSGRUNNLAG, // Fastsette endring i beregningsgrunnlag
    UDEFINERT,
    ;

    @JsonCreator
    public static FaktaOmBeregningTilfelle fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        if (KodeKonstanter.UDEFINERT.equals(kode)) {
            return UDEFINERT;
        }
        return FaktaOmBeregningTilfelle.valueOf(kode);
    }

    @Override
    @JsonValue
    public String getKode() {
        return this == UDEFINERT ? KodeKonstanter.UDEFINERT : name();
    }


}
