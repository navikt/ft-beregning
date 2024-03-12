package no.nav.folketrygdloven.kalkulator.adapter.vltilregelmodell.kodeverk;


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import no.nav.folketrygdloven.kalkulus.kodeverk.PeriodeÅrsak;


public class MapPeriodeÅrsakFraVlTilRegel {

    private static final Map<PeriodeÅrsak, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak> PERIODE_ÅRSAK_MAP;

    static {
        Map<PeriodeÅrsak, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak> mapPeriodeÅrsak = new LinkedHashMap<>();

        mapPeriodeÅrsak.put(PeriodeÅrsak.NATURALYTELSE_BORTFALT, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.NATURALYTELSE_BORTFALT);
        mapPeriodeÅrsak.put(PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.ARBEIDSFORHOLD_AVSLUTTET);
        mapPeriodeÅrsak.put(PeriodeÅrsak.NATURALYTELSE_TILKOMMER, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.NATURALYTELSE_TILKOMMER);
        mapPeriodeÅrsak.put(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV);
        mapPeriodeÅrsak.put(PeriodeÅrsak.REFUSJON_OPPHØRER, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.REFUSJON_OPPHØRER);
        mapPeriodeÅrsak.put(PeriodeÅrsak.GRADERING, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.GRADERING);
        mapPeriodeÅrsak.put(PeriodeÅrsak.GRADERING_OPPHØRER, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.GRADERING_OPPHØRER);
        mapPeriodeÅrsak.put(PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR);
        mapPeriodeÅrsak.put(PeriodeÅrsak.TILKOMMET_INNTEKT, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.TILKOMMET_INNTEKT);
        mapPeriodeÅrsak.put(PeriodeÅrsak.REFUSJON_AVSLÅTT, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.REFUSJON_AVSLÅTT);

        PERIODE_ÅRSAK_MAP = Collections.unmodifiableMap(mapPeriodeÅrsak);

    }

    private MapPeriodeÅrsakFraVlTilRegel() {
        // skjul public constructor
    }

    public static no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak map(PeriodeÅrsak periodeÅrsak) {
        no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak regelmodellPeriodeÅrsak = PERIODE_ÅRSAK_MAP.getOrDefault(periodeÅrsak, no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak.UDEFINERT);
        if (regelmodellPeriodeÅrsak == null) {
            throw new IllegalStateException("Ukjent PeriodeÅrsak: (" + periodeÅrsak + ").");
        }
        return regelmodellPeriodeÅrsak;
    }
}
