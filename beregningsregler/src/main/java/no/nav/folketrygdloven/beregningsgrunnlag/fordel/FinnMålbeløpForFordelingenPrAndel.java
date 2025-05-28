package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelteAndelerModell;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class FinnMålbeløpForFordelingenPrAndel extends LeafSpecification<FordelModell> {

    static final String ID = "FINN_BELØP_SOM_SKAL_FORDELES_TIL_ANDEL";
    static final String BESKRIVELSE = "Finner beløpet som skal fordeles til andel ved å se på forholdet mellom total foreslått brutto og fraksjon av brutto for andelen";

    public FinnMålbeløpForFordelingenPrAndel() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(FordelModell modell) {
	    Map<String, Object> resultater = new HashMap<>();
	    BigDecimal totalBruttoSomSkalFordeles = finnTotalBrutto(modell.getInput());
	    resultater.put("totalBruttoSomSkalFordeles", totalBruttoSomSkalFordeles);
		modell.getMellomregninger().forEach(mellomregning -> {
			BigDecimal målbeløp = finnMålbeløp(totalBruttoSomSkalFordeles, mellomregning);
			mellomregning.setMålbeløp(målbeløp);
			resultater.put("andel", mellomregning.getInputAndel().getBeskrivelse());
			resultater.put("beløpSomSkalFordelesTilAndel", målbeløp);
		});
	    return beregnet(resultater);
	}

	private BigDecimal finnMålbeløp(BigDecimal totalBruttoSomSkalFordeles, FordelteAndelerModell mellomregning) {
		return mellomregning.getFraksjonAvBrutto().multiply(totalBruttoSomSkalFordeles);
	}

	private BigDecimal finnTotalBrutto(FordelPeriodeModell input) {
		// Hvis foreslått ikke er satt betyr det tilkommet arbeidsforhold, de bidrar ikke til brutto og setter derfor 0 for disse
		return input.getAndeler().stream()
				.map(andel -> andel.getGradertForeslåttPrÅr().orElse(BigDecimal.ZERO))
				.reduce(BigDecimal::add)
				.orElseThrow();
	}
}
