package no.nav.folketrygdloven.kalkulator.adapter.regelmodelltilvl.kodeverk;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


public class MapInntektskategoriRegelTilVL {
    private static final Map<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori, Inntektskategori> INNTEKTSKATEGORI_MAP;

    private MapInntektskategoriRegelTilVL() {}

    static {
        Map<no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori, Inntektskategori> map = new EnumMap<>(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.class);
        map.put(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.ARBEIDSTAKER, Inntektskategori.ARBEIDSTAKER);
        map.put(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.FRILANSER, Inntektskategori.FRILANSER);
        map.put(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        map.put(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.DAGPENGER, Inntektskategori.DAGPENGER);
        map.put(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.ARBEIDSAVKLARINGSPENGER, Inntektskategori.ARBEIDSAVKLARINGSPENGER);
        map.put(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.SJØMANN, Inntektskategori.SJØMANN);
        map.put(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.DAGMAMMA, Inntektskategori.DAGMAMMA);
        map.put(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.JORDBRUKER, Inntektskategori.JORDBRUKER);
        map.put(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.FISKER, Inntektskategori.FISKER);
        map.put(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER, Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER);
        map.put(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.UDEFINERT, Inntektskategori.UDEFINERT);
        INNTEKTSKATEGORI_MAP = Collections.unmodifiableMap(map);
    }

    public static Inntektskategori map(no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori inntektskategoriRegel) {
        if (!INNTEKTSKATEGORI_MAP.containsKey(inntektskategoriRegel)) {
            throw new IllegalArgumentException("Utviklerfeil: Mangler mapping fra regel til VL for Inntektskategori " + inntektskategoriRegel);
        }
        return INNTEKTSKATEGORI_MAP.get(inntektskategoriRegel);
    }
}
