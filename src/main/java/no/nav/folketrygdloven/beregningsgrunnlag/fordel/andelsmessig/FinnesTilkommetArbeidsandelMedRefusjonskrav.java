package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.math.BigDecimal;

/**
 * Sjekker om det finnes et tilkommet arbeidsforhold med refusjonskrav.
 *
 */
class FinnesTilkommetArbeidsandelMedRefusjonskrav extends LeafSpecification<FordelModell> {

    static final String ID = "FINNES_TILKOMMET_REFUSJONSKRAV ";
    static final String BESKRIVELSE = "Er det et tilkommet refusjonskrav i beregningsgrunnlaget?";

    FinnesTilkommetArbeidsandelMedRefusjonskrav() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(FordelModell grunnlag) {
	    var finnesTilkommetAndelMedRefusjonskrav = grunnlag.getInput().getAlleAndelerForStatus(AktivitetStatus.AT).stream()
			    .filter(andel -> andel.getForeslåttPrÅr().isEmpty())
			    .anyMatch(andel -> andel.getGjeldendeRefusjonPrÅr().isPresent() && andel.getGjeldendeRefusjonPrÅr().get().compareTo(BigDecimal.ZERO) > 0);
	    return finnesTilkommetAndelMedRefusjonskrav ? ja() : nei();
	}
}
