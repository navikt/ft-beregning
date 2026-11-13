package no.nav.folketrygdloven.kalkulator.steg.refusjon;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.refusjon.FordelingGrunnlag;
import no.nav.folketrygdloven.kalkulator.output.BeregningsgrunnlagRegelResultat;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.FordelPerioderTjeneste;

public final class RefusjonsperiodiseringTjeneste {

    private RefusjonsperiodiseringTjeneste() {
    }

    public static Resultat fastsettPerioderForRefusjon(BeregningsgrunnlagInput input) {
        if (input.isEnabled(BeregningsgrunnlagInput.REFUSJON_TOGGLE, false)) {
            var resultat = new RefusjonsgrunnlagTjeneste().fastsettRefusjonsgrunnlag(input);
            return new Resultat(new BeregningsgrunnlagRegelResultat(resultat.beregningsgrunnlag(), resultat.regelsporinger()),
                resultat.fordelingGrunnlag());
        } else {
            return new Resultat(new FordelPerioderTjeneste().fastsettPerioderForRefusjon(input), null);
        }
    }

    public record Resultat(BeregningsgrunnlagRegelResultat regelResultat, FordelingGrunnlag fordelingGrunnlag) {
    }
}
