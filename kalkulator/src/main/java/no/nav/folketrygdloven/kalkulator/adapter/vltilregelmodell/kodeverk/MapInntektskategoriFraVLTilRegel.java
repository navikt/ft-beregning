package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import no.nav.folketrygdloven.kalkulus.kodeverk.Inntektskategori;


public class MapInntektskategoriFraVLTilRegel {

    private static final Map<Inntektskategori, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori> MAP_INNTEKTSKATEGORI;

    static {
        Map<Inntektskategori, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori> mapInntektskategori = new LinkedHashMap<>();
        mapInntektskategori.put(Inntektskategori.ARBEIDSAVKLARINGSPENGER, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.ARBEIDSAVKLARINGSPENGER);
        mapInntektskategori.put(Inntektskategori.ARBEIDSTAKER, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.ARBEIDSTAKER);
        mapInntektskategori.put(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER);
        mapInntektskategori.put(Inntektskategori.DAGMAMMA, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.DAGMAMMA);
        mapInntektskategori.put(Inntektskategori.DAGPENGER, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.DAGPENGER);
        mapInntektskategori.put(Inntektskategori.FISKER, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.FISKER);
        mapInntektskategori.put(Inntektskategori.FRILANSER, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.FRILANSER);
        mapInntektskategori.put(Inntektskategori.JORDBRUKER, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.JORDBRUKER);
        mapInntektskategori.put(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        mapInntektskategori.put(Inntektskategori.SJØMANN, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.SJØMANN);
        mapInntektskategori.put(Inntektskategori.UDEFINERT, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori.UDEFINERT);

        MAP_INNTEKTSKATEGORI = Collections.unmodifiableMap(mapInntektskategori);
    }

    private MapInntektskategoriFraVLTilRegel() {
        // skjul public constructor
    }

    public static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori map(Inntektskategori inntektskategori) {
        if (MAP_INNTEKTSKATEGORI.containsKey(inntektskategori)) {
            return MAP_INNTEKTSKATEGORI.get(inntektskategori);
        }
        throw new IllegalStateException("Inntektskategori (" + inntektskategori + ") finnes ikke i mappingen.");
    }
}
