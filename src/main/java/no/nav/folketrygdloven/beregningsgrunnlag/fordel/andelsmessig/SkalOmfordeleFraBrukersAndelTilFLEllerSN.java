package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

/**
 * Bestemmer om det skal omfordeles fra en brukers andel til FL eller SN.
 *
 * Se {@link OmfordelFraBrukersAndel} for en forklaring på hvilke situasjoner vi forventer å finne en brukers andel.
 *
 */
class SkalOmfordeleFraBrukersAndelTilFLEllerSN extends LeafSpecification<FordelModell> {

    static final String ID = "SKAL_OMFORDELE_FRA_BA";
    static final String BESKRIVELSE = "Har brukers andel og søkt for FL eller SN?";

    SkalOmfordeleFraBrukersAndelTilFLEllerSN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(FordelModell grunnlag) {
		    var harBrukersAndel = harBrukersAndelMedBeregningsgrunnlag(grunnlag);
		    boolean harSøktForFLEllerSN = harSøktFLEllerSN(grunnlag);
		    return harBrukersAndel && harSøktForFLEllerSN ? ja() : nei();
    }

	private boolean harSøktFLEllerSN(FordelModell grunnlag) {
		boolean harSøktFrilans = grunnlag.getInput().getEnesteAndelForStatus(AktivitetStatus.FL).map(FordelAndelModell::erSøktYtelseFor).orElse(false);
		boolean harSøktSN = grunnlag.getInput().getEnesteAndelForStatus(AktivitetStatus.SN).map(FordelAndelModell::erSøktYtelseFor).orElse(false);
		return harSøktFrilans || harSøktSN;
	}

	private boolean harBrukersAndelMedBeregningsgrunnlag(FordelModell grunnlag) {
		var brukersAndel = grunnlag.getInput().getEnesteAndelForStatus(AktivitetStatus.BA);
		return brukersAndel.map(ba -> ba.getBruttoPrÅr().orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0).orElse(false);
	}

}
