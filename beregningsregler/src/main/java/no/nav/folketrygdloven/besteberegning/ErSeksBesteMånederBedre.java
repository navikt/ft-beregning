package no.nav.folketrygdloven.besteberegning;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.besteberegning.modell.BesteberegningRegelmodell;
import no.nav.folketrygdloven.besteberegning.modell.output.BesteberegnetAndel;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class ErSeksBesteMånederBedre extends LeafSpecification<BesteberegningRegelmodell> {

	// Trenger dokumentasjon på confluence og referanse til denne
	public static final String ID = "14-7-3.3";
	public static final String BESKRIVELSE = "Er seks beste måneder bedre enn beregning på skjæringstidspunktet?";


	public ErSeksBesteMånederBedre() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(BesteberegningRegelmodell regelmodell) {
        var output = regelmodell.getOutput();
        var besteberegnedeAndeler = output.getBesteberegnetGrunnlag().getBesteberegnetAndelList();
        var besteberegnetBeløp = besteberegnedeAndeler.stream()
				.map(BesteberegnetAndel::getBesteberegnetPrÅr)
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
        var beregnetBeløp = regelmodell.getInput().getBeregnetGrunnlag();

        var besteberegningGirHøyereBeregning = besteberegnetBeløp.compareTo(beregnetBeløp) > 0;
		output.setSkalBeregnesEtterSeksBesteMåneder(besteberegningGirHøyereBeregning);

		Map<String, Object> resultater = new HashMap<>();
		resultater.put("besteberegnetBeløp", besteberegnetBeløp);
		resultater.put("beregnetBeløp", beregnetBeløp);
		resultater.put("besteberegningGirHøyereBeregning", besteberegningGirHøyereBeregning);
		return beregnet(resultater);
	}


}
