package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

/**
 * Bestemmer om det skal omfordeles fra en brukers andel til FL eller SN.
 *
 * Se {@link OmfordelFraBrukersAndel} for en forklaring på hvilke situasjoner vi forventer å finne en brukers andel.
 *
 */
class SkalOmfordeleFraBrukersAndelTilFLEllerSN extends LeafSpecification<FordelPeriodeModell> {

    static final String ID = "SKAL_OMFORDELE_FRA_BA";
    static final String BESKRIVELSE = "Har brukers andel og søkt for FL eller SN?";

    SkalOmfordeleFraBrukersAndelTilFLEllerSN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(FordelPeriodeModell grunnlag) {
		    var harBrukersAndel = harBrukersAndelMedBeregningsgrunnlag(grunnlag);
		    boolean harSøktForFLEllerSN = harSøktFLEllerSN(grunnlag);
		    return harBrukersAndel && harSøktForFLEllerSN ? ja() : nei();
    }

	private boolean harSøktFLEllerSN(FordelPeriodeModell grunnlag) {
		boolean harSøktFrilans = grunnlag.getEnesteAndelForStatus(AktivitetStatus.FL).map(FordelAndelModell::erSøktYtelseFor).orElse(false);
		boolean harSøktSN = grunnlag.getEnesteAndelForStatus(AktivitetStatus.SN).map(FordelAndelModell::erSøktYtelseFor).orElse(false);
		return harSøktFrilans || harSøktSN;
	}

	private boolean harBrukersAndelMedBeregningsgrunnlag(FordelPeriodeModell grunnlag) {
		var brukersAndel = grunnlag.getEnesteAndelForStatus(AktivitetStatus.BA);
		return brukersAndel.map(ba -> ba.getBruttoPrÅr().orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0).orElse(false);
	}

}
