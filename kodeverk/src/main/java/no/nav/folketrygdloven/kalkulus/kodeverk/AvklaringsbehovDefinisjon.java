package no.nav.folketrygdloven.kalkulus.kodeverk;

import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovType.AUTOPUNKT;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovType.MANUELL;
import static no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovType.OVERSTYRING;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Definerer avklaringsbehov som kan utledes i beregning.
 */
public
enum AvklaringsbehovDefinisjon implements Kodeverdi, DatabaseKode, KontraktKode {

    // 5000 vanlig saksbehandlig
    FASTSETT_BG_AT_FL(KodeKonstanter.AB_FASTSETT_BG_AT_FL, MANUELL, BeregningSteg.FORS_BERGRUNN), // Fastsette beregningsgrunnlag for arbeidstaker/frilanser skjønnsmessig
    VURDER_VARIG_ENDRT_NYOPPSTR_NAERNG_SN(KodeKonstanter.AB_VURDER_VARIG_ENDRT_NYOPPSTR_NAERNG_SN, MANUELL, BeregningSteg.FORS_BERGRUNN_2), // Vurder varig endret/nyoppstartet næring selvstendig næringsdrivende
    VURDER_VARIG_ENDRT_ARB_SITSJN_MDL_INAKTV(KodeKonstanter.AB_VURDER_VARIG_ENDRT_ARB_SITSJN_MDL_INAKTV, MANUELL, BeregningSteg.FORS_BERGRUNN_2), // Vurder varig endret arbeidssituasjon for bruker som er midlertidig inaktiv
    FORDEL_BG(KodeKonstanter.AB_FORDEL_BG, MANUELL, BeregningSteg.FORDEL_BERGRUNN), // Fordel beregningsgrunnlag
    FASTSETT_BG_TB_ARB(KodeKonstanter.AB_FASTSETT_BG_TB_ARB, MANUELL, BeregningSteg.FORS_BERGRUNN), // Fastsett beregningsgrunnlag for tidsbegrenset arbeidsforhold
    VURDER_NYTT_INNTKTSFRHLD(KodeKonstanter.AB_VURDER_NYTT_INNTKTSFRHLD, MANUELL, BeregningSteg.VURDER_TILKOMMET_INNTEKT), // Vurder nytt inntektsforhold
    VURDER_REPRESENTERER_STORTINGET(KodeKonstanter.AB_VURDER_REPRESENTERER_STORTINGET, MANUELL, BeregningSteg.FORDEL_BERGRUNN), // Vurder om bruker representerer stortinget i perioden
    FASTSETT_BG_SN_NY_I_ARB_LIVT(KodeKonstanter.AB_FASTSETT_BG_SN_NY_I_ARB_LIVT, MANUELL, BeregningSteg.FORS_BERGRUNN_2), // Fastsett beregningsgrunnlag for SN som er ny i arbeidslivet
    AVKLAR_AKTIVITETER(KodeKonstanter.AB_AVKLAR_AKTIVITETER, MANUELL, BeregningSteg.FASTSETT_STP_BER), // Avklar aktivitet for beregning
    VURDER_FAKTA_ATFL_SN(KodeKonstanter.AB_VURDER_FAKTA_ATFL_SN, MANUELL, BeregningSteg.KOFAKBER), // Vurder fakta for arbeidstaker, frilans og selvstendig næringsdrivende
    VURDER_REFUSJONSKRAV(KodeKonstanter.AB_VURDER_REFUSJONSKRAV, MANUELL, BeregningSteg.VURDER_REF_BERGRUNN), // Vurder refusjonskrav for beregningen

    // 6000 overstyring
    OVST_BEREGNINGSAKTIVITETER(KodeKonstanter.OVST_BEREGNINGSAKTIVITETER, OVERSTYRING, BeregningSteg.FASTSETT_STP_BER), // Overstyring av beregningsaktiviteter
    OVST_INNTEKT(KodeKonstanter.OVST_INNTEKT, OVERSTYRING, BeregningSteg.KOFAKBER), // Overstyring av beregningsgrunnlag

    // 7000 automatisk satt på vent
    AUTO_VENT_PÅ_INNTKT_RAP_FRST("AUTO_VENT_PAA_INNTKT_RAP_FRST", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER), // Vent på rapporteringsfrist for inntekt
    AUTO_VENT_PÅ_SISTE_AAP_DP_MELDKRT("AUTO_VENT_PAA_SISTE_AAP_DP_MELDKRT", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER), // Vent på siste meldekort for AAP eller DP-mottaker

    // 8000 frisinn
    AUTO_VENT_FRISINN("AUTO_VENT_FRISINN", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER), // Sak settes på vent på grunn av manglende funksjonalitet
    INGEN_AKTIVITETER("INGEN_AKTIVITETER", AUTOPUNKT, BeregningSteg.FASTSETT_STP_BER) // Gir avslag
    ;
    private static final Map<String, AvklaringsbehovDefinisjon> KODER = new LinkedHashMap<>();

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
    private final AvklaringsbehovType avklaringsbehovType;
    @JsonIgnore
    private final BeregningSteg stegFunnet;


    AvklaringsbehovDefinisjon(String kode, AvklaringsbehovType avklaringsbehovType, BeregningSteg stegFunnet) {
        this.kode = Objects.requireNonNull(kode);
        this.stegFunnet = stegFunnet;
        this.avklaringsbehovType = avklaringsbehovType;
    }

    @Override
    public String getKode() {
        return kode;
    }


    public AvklaringsbehovType getAvklaringsbehovType() {
        return avklaringsbehovType;
    }

    public BeregningSteg getStegFunnet() {
        return stegFunnet;
    }

    public static AvklaringsbehovDefinisjon fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        var ny = KODER.get(kode);
        if (ny == null) {
            throw new IllegalArgumentException("Ukjent BeregningAvklaringsbehovDefinisjon: " + kode);
        }
        return ny;
    }

    public boolean erVentepunkt() {
        return AUTOPUNKT.equals(this.avklaringsbehovType);
    }

    public boolean erOverstyring() {
        return OVERSTYRING.equals(this.avklaringsbehovType);
    }
}
