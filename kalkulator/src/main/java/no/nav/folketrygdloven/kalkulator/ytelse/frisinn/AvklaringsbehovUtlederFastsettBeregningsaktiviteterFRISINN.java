package no.nav.folketrygdloven.kalkulator.ytelse.frisinn;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.output.BeregningAvklaringsbehovResultat;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulus.kodeverk.AvklaringsbehovDefinisjon;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningVenteårsak;

public class AvklaringsbehovUtlederFastsettBeregningsaktiviteterFRISINN  {

    public List<BeregningAvklaringsbehovResultat> utledAvklaringsbehov(BeregningsgrunnlagRegelResultat regelResultat) {
        if (regelResultat.getBeregningsgrunnlagHvisFinnes().isEmpty()) {
            if (regelResultat.getAvklaringsbehov().stream().anyMatch(bar -> bar.getBeregningAvklaringsbehovDefinisjon().equals(AvklaringsbehovDefinisjon.AUTO_VENT_FRISINN))) {
                return List.of(BeregningAvklaringsbehovResultat.opprettMedFristFor(
                        AvklaringsbehovDefinisjon.AUTO_VENT_FRISINN,
                        BeregningVenteårsak.INGEN_PERIODE_UTEN_YTELSE,
                        LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT)));
            }
            if (regelResultat.getAvklaringsbehov().stream().anyMatch(bar -> bar.getBeregningAvklaringsbehovDefinisjon().equals(AvklaringsbehovDefinisjon.INGEN_AKTIVITETER))) {
                return List.of(BeregningAvklaringsbehovResultat.opprettMedFristFor(
                        AvklaringsbehovDefinisjon.AUTO_VENT_FRISINN,
                        BeregningVenteårsak.INGEN_AKTIVITETER,
                        LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT)));
            }
        }
        return Collections.emptyList();
    }


}
