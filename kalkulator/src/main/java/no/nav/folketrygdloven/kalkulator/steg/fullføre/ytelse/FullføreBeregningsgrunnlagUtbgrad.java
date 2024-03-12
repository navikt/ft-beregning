package no.nav.folketrygdloven.kalkulator.steg.fullføre.ytelse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.RegelResultat;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.Beregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.FullføreBeregningsgrunnlag;
import no.nav.folketrygdloven.kalkulator.steg.fullføre.FullføreBeregningsgrunnlagUtils;
import no.nav.folketrygdloven.regelmodelloversetter.KalkulusRegler;

public class FullføreBeregningsgrunnlagUtbgrad extends FullføreBeregningsgrunnlag {

    public FullføreBeregningsgrunnlagUtbgrad() {
        super();
    }

    @Override
    protected List<RegelResultat> evaluerRegelmodell(Beregningsgrunnlag beregningsgrunnlagRegel, BeregningsgrunnlagInput bgInput) {
        String input = FullføreBeregningsgrunnlagUtils.toJson(beregningsgrunnlagRegel);
        // Regel for å finne grenseverdi for andre gjennomkjøring
        List<String> sporingerFinnGrenseverdi = kjørRegelFinnGrenseverdi(beregningsgrunnlagRegel);

        //Andre gjennomkjøring av regel
        List<RegelResultat> regelResultater = kjørRegelFullførberegningsgrunnlag(beregningsgrunnlagRegel);

        return FullføreBeregningsgrunnlagUtils.leggTilSporingerForFinnGrenseverdi(input, sporingerFinnGrenseverdi, regelResultater);
    }

    protected List<RegelResultat> kjørRegelFullførberegningsgrunnlag(Beregningsgrunnlag beregningsgrunnlagRegel) {
        List<RegelResultat> regelResultater = new ArrayList<>();
        for (BeregningsgrunnlagPeriode periode : beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder()) {
            regelResultater.add(KalkulusRegler.fullføreBeregningsgrunnlag(periode));
        }
        return regelResultater;
    }

    protected List<String> kjørRegelFinnGrenseverdi(Beregningsgrunnlag beregningsgrunnlagRegel) {
        return beregningsgrunnlagRegel.getBeregningsgrunnlagPerioder().stream()
                .map(periode -> KalkulusRegler.finnGrenseverdi(periode).sporing().sporing())
                .collect(Collectors.toList());
    }

}
