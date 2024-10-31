package no.nav.folketrygdloven.kalkulator.steg.tilkommetInntekt;

import no.nav.folketrygdloven.kalkulator.input.BeregningsgrunnlagInput;
import no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag.BeregningsgrunnlagDto;
import no.nav.folketrygdloven.kalkulator.steg.fordeling.periodisering.PeriodiserForAktivitetsgradTjeneste;


public class TilkommetInntektTjeneste {

	private final TilkommetInntektPeriodeTjeneste periodeTjeneste = new TilkommetInntektPeriodeTjeneste();

	public BeregningsgrunnlagDto vurderTilkommetInntekt(BeregningsgrunnlagInput input) {
		var splittForAktivitetsgrad = PeriodiserForAktivitetsgradTjeneste.splittVedEndringIAktivitetsgrad(input.getBeregningsgrunnlag(), input.getYtelsespesifiktGrunnlag());
		return periodeTjeneste.splittPerioderVedTilkommetInntekt(input, splittForAktivitetsgrad);

	}

}
