package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.FordelPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.ForlengelsePeriodeTjeneste;

public class VurderRefusjonBeregningsgrunnlagFRISINN {

    public BeregningsgrunnlagRegelResultat vurderRefusjon(BeregningsgrunnlagInput input) {
        BeregningsgrunnlagRegelResultat resultatFraRefusjonPeriodisering = new FordelPerioderTjeneste().fastsettPerioderForRefusjon(input);
        BeregningsgrunnlagRegelResultat resultatFraPeriodisering = new FordelPerioderTjeneste().fastsettPerioderForUtbetalingsgradEllerGradering(input, resultatFraRefusjonPeriodisering.getBeregningsgrunnlag());
        var splittetVedForlengelse = ForlengelsePeriodeTjeneste.splittVedStartAvForlengelse(input, resultatFraPeriodisering.getBeregningsgrunnlag());
        return new BeregningsgrunnlagRegelResultat(splittetVedForlengelse,
                List.of(),
                RegelSporingAggregat.konkatiner(resultatFraRefusjonPeriodisering.getRegelsporinger().orElse(null), resultatFraPeriodisering.getRegelsporinger().orElse(null)));
    }
}
