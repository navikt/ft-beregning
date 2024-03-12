package no.nav.folketrygdloven.kalkulator.steg.refusjon.ytelse;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.FordelPerioderTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.PeriodiserForAktivitetsgradTjeneste;
import no.nav.folketrygdloven.kalkulator.steg.refusjon.ForlengelsePeriodeTjeneste;

public class VurderRefusjonBeregningsgrunnlagPleiepenger {

    public BeregningsgrunnlagRegelResultat vurderRefusjon(BeregningsgrunnlagInput input) {
        BeregningsgrunnlagRegelResultat resultatFraRefusjonPeriodisering = new FordelPerioderTjeneste().fastsettPerioderForRefusjon(input);
        BeregningsgrunnlagRegelResultat resultatFraPeriodisering = new FordelPerioderTjeneste().fastsettPerioderForUtbetalingsgradEllerGradering(input, resultatFraRefusjonPeriodisering.getBeregningsgrunnlag());
        var splittetVedForlengelse = ForlengelsePeriodeTjeneste.splittVedStartAvForlengelse(input, resultatFraPeriodisering.getBeregningsgrunnlag());
        // Kjører splitt pga aktivitetsgrad her i tillegg til vurder tilkommet inntekt siden det ikke er alle som kjører det steget
        var splittForAktivitetsgrad = PeriodiserForAktivitetsgradTjeneste.splittVedEndringIAktivitetsgrad(splittetVedForlengelse, input.getYtelsespesifiktGrunnlag());
        var avklaringsbehov = AvklaringsbehovutledertjenesteVurderRefusjon.utledAvklaringsbehov(input, splittetVedForlengelse);
        return new BeregningsgrunnlagRegelResultat(splittForAktivitetsgrad,
                avklaringsbehov,
                RegelSporingAggregat.konkatiner(resultatFraRefusjonPeriodisering.getRegelsporinger().orElse(null), resultatFraPeriodisering.getRegelsporinger().orElse(null)));
    }
}
