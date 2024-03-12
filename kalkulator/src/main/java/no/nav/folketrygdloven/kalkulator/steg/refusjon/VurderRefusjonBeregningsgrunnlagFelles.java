package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.FordelPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse.AvklaringsbehovutledertjenesteVurderRefusjon;

public class VurderRefusjonBeregningsgrunnlagFelles {

    public BeregningsgrunnlagRegelResultat vurderRefusjon(BeregningsgrunnlagInput input) {
        BeregningsgrunnlagRegelResultat resultatFraRefusjonPeriodisering = new FordelPerioderTjeneste().fastsettPerioderForRefusjon(input);
        BeregningsgrunnlagRegelResultat resultatFraPeriodisering = new FordelPerioderTjeneste().fastsettPerioderForUtbetalingsgradEllerGradering(input, resultatFraRefusjonPeriodisering.getBeregningsgrunnlag());
        var splittetVedForlengelse = ForlengelsePeriodeTjeneste.splittVedStartAvForlengelse(input, resultatFraPeriodisering.getBeregningsgrunnlag());
        var avklaringsbehov = AvklaringsbehovutledertjenesteVurderRefusjon.utledAvklaringsbehov(input, splittetVedForlengelse);
        return new BeregningsgrunnlagRegelResultat(splittetVedForlengelse,
                avklaringsbehov,
                RegelSporingAggregat.konkatiner(resultatFraRefusjonPeriodisering.getRegelsporinger().orElse(null), resultatFraPeriodisering.getRegelsporinger().orElse(null)));
    }
}
