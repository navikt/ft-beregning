package no.nav.folketrygdloven.kalkulator.felles.inntektgradering;

import java.util.ArrayList;
import java.util.List;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingAggregat;
import no.nav.folketrygdloven.kalkulator.output.RegelSporingPeriode;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.FullføreBeregningsgrunnlagUtils;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningsgrunnlagPeriodeRegelType;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

public class FinnUttaksgradInntektsgradering {

    private FinnUttaksgradInntektsgradering() {
    }

    public static BeregningsgrunnlagRegelResultat finnInntektsgradering(BeregningsgrunnlagInput input) {
        var beregningsgrunnlagRegel = new MapBeregningsgrunnlagFraVLTilRegel().map(input, input.getBeregningsgrunnlag());
        List<RegelSporingPeriode> regelsporinger = new ArrayList<>();
        for (var regelPeriode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            if (regelPeriode.getTilkommetInntektsforholdListe() != null && !regelPeriode.getTilkommetInntektsforholdListe().isEmpty()) {
                var regelResultat = KalkulusRegler.finnGrenseverdi(regelPeriode);
                regelsporinger.add(new RegelSporingPeriode(regelResultat.sporing().sporing(), regelResultat.sporing().input(),
                    Intervall.fraOgMedTilOgMed(regelPeriode.getPeriodeFom(), regelPeriode.getPeriodeTom()),
                    BeregningsgrunnlagPeriodeRegelType.FINN_GRADERING_VED_TILKOMMET_INNTEKT, regelResultat.versjon()));
            }

        }
        var oppdatertMedInntektsgradering = FullføreBeregningsgrunnlagUtils.mapBeregningsgrunnlagFraRegelTilVL(beregningsgrunnlagRegel, input.getBeregningsgrunnlag());
        return new BeregningsgrunnlagRegelResultat(oppdatertMedInntektsgradering, new RegelSporingAggregat(regelsporinger));
    }

}
