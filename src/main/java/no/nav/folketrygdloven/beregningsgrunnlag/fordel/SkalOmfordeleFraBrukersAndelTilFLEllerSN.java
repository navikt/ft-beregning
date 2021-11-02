package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

/**
 * Bestemmer om det skal omfordeles fra en brukers andel til FL eller SN.
 *
 * Se {@link OmfordelFraBrukersAndel} for en forklaring på hvilke situasjoner vi forventer å finne en brukers andel.
 *
 */
class SkalOmfordeleFraBrukersAndelTilFLEllerSN extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "SKAL_OMFORDELE_FRA_BA";
    static final String BESKRIVELSE = "Har brukers andel og søkt for FL eller SN?";

    SkalOmfordeleFraBrukersAndelTilFLEllerSN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		    var harBrukersAndel = harBrukersAndelMedBeregningsgrunnlag(grunnlag);
		    boolean harSøktForFLEllerSN = harSøktFLEllerSN(grunnlag);
		    return harBrukersAndel && harSøktForFLEllerSN ? ja() : nei();
    }

	private boolean harSøktFLEllerSN(BeregningsgrunnlagPeriode grunnlag) {
		var atflStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL);
		boolean harSøktFrilans = atflStatus != null && atflStatus.getFrilansArbeidsforhold().map(BeregningsgrunnlagPrArbeidsforhold::getErSøktYtelseFor).orElse(false);
		var snStatus = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.SN);
		boolean harSøktSN = snStatus != null && snStatus.erSøktYtelseFor();
		return harSøktFrilans || harSøktSN;
	}

	private boolean harBrukersAndelMedBeregningsgrunnlag(BeregningsgrunnlagPeriode grunnlag) {
		var brukersAndel = grunnlag.getBeregningsgrunnlagPrStatus(AktivitetStatus.BA);
		if (brukersAndel == null) {
			return false;
		}
		return brukersAndel.getBruttoPrÅr().compareTo(BigDecimal.ZERO) > 0;
	}

}
