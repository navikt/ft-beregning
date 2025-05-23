package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class FinnesTilkommetArbeidsandelMedRefusjonskrav extends LeafSpecification<FordelModell> {

    static final String ID = "FINNES_TILKOMMET_REFUSJONSKRAV ";
    static final String BESKRIVELSE = "Er det et tilkommet refusjonskrav i beregningsgrunnlaget?";

    FinnesTilkommetArbeidsandelMedRefusjonskrav() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(FordelModell modell) {
	    var finnesTilkommetAndelMedRefusjonskrav = modell.getInput().getAlleAndelerForStatus(AktivitetStatus.AT).stream()
			    .filter(andel -> andel.getForeslåttPrÅr().isEmpty())
			    .anyMatch(andel -> andel.getGjeldendeRefusjonPrÅr().isPresent() && andel.getGjeldendeRefusjonPrÅr().get().compareTo(BigDecimal.ZERO) > 0);
	    return finnesTilkommetAndelMedRefusjonskrav ? ja() : nei();
	}
}
