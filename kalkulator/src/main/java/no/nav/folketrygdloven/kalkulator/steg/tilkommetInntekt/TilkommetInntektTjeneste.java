package no.nav.folketrygdloven.kalkulator.steg.tilkommetInntekt;

import no.nav.folketrygdloven.kalkulator.KonfigurasjonVerdi;
import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.PeriodiserForAktivitetsgradTjeneste;


public class TilkommetInntektTjeneste {

    private static final boolean GRADERING_MOT_INNTEKT_ENABLED = KonfigurasjonVerdi.instance().get("GRADERING_MOT_INNTEKT", false);

    private final TilkommetInntektPeriodeTjeneste periodeTjeneste = new TilkommetInntektPeriodeTjeneste();

    public BeregningsgrunnlagDto vurderTilkommetInntekt(BeregningsgrunnlagInput input) {
        if (!GRADERING_MOT_INNTEKT_ENABLED) {
            return input.getBeregningsgrunnlag();
        } else {
            var splittForAktivitetsgrad = PeriodiserForAktivitetsgradTjeneste.splittVedEndringIAktivitetsgrad(input.getBeregningsgrunnlag(), input.getYtelsespesifiktGrunnlag());
            return periodeTjeneste.splittPerioderVedTilkommetInntekt(input, splittForAktivitetsgrad);
        }
    }

}
