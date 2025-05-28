package no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.MidlertidigInaktivType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FinnGrenseverdi.ID)
public class FinnGrenseverdi extends LeafSpecification<BeregningsgrunnlagPeriode> {

	public static final String ID = "FP_BR 6.2_med_fordeling";
	public static final String BESKRIVELSE = "Finn grenseverdi";

	public FinnGrenseverdi() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		Map<String, Object> resultater = new HashMap<>();

		//gradering mot uttak
        var summerAvkortetGradertMotUttak = summerAvkortetGradertMotUttak(grunnlag);
		var grenseverdi = summerAvkortetGradertMotUttak;
        var sumAvkortet = summerAvkortet(grunnlag);
        var totalUtbetalingsgradFraUttak = sumAvkortet.signum() != 0 ? summerAvkortetGradertMotUttak.divide(sumAvkortet, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
		grunnlag.setTotalUtbetalingsgradFraUttak(totalUtbetalingsgradFraUttak);
		resultater.put("totalUtbetalingsgradFraUttak", totalUtbetalingsgradFraUttak);

		//hvis §8-47a, skaler med fast faktor
		var erInaktivTypeA = MidlertidigInaktivType.A.equals(grunnlag.getBeregningsgrunnlag().getMidlertidigInaktivType());
		if (erInaktivTypeA) {
            var reduksjonsfaktor = grunnlag.getBeregningsgrunnlag().getMidlertidigInaktivTypeAReduksjonsfaktor();
			grenseverdi = grenseverdi.multiply(reduksjonsfaktor);
			resultater.put("grad847a", reduksjonsfaktor);
			grunnlag.setReduksjonsfaktorInaktivTypeA(reduksjonsfaktor);

            var justertTotalUtbetalingsgradFraUttak = totalUtbetalingsgradFraUttak.multiply(reduksjonsfaktor);
			grunnlag.setTotalUtbetalingsgradFraUttak(justertTotalUtbetalingsgradFraUttak);
			resultater.put("totalUtbetalingsgradFraUttak", justertTotalUtbetalingsgradFraUttak);
		}

		resultater.put("grenseverdi", grenseverdi);
		grunnlag.setGrenseverdi(grenseverdi);
        var resultat = ja();
		resultat.setEvaluationProperties(resultater);
		return resultat;

	}

	private static BigDecimal summerAvkortetGradertMotUttak(BeregningsgrunnlagPeriode grunnlag) {
        var sum = BigDecimal.ZERO;
		for (var bps : grunnlag.getBeregningsgrunnlagPrStatus()) {
			if (bps.erArbeidstakerEllerFrilanser()) {
				sum = sum.add(bps.getArbeidsforhold().stream()
						.map(arb -> arb.getAndelsmessigFørGraderingPrAar().multiply(arb.getUtbetalingsprosent().scaleByPowerOfTen(-2)))
						.reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
			} else {
				sum = sum.add(bps.getAndelsmessigFørGraderingPrAar().multiply(bps.getUtbetalingsprosent().scaleByPowerOfTen(-2)));
			}
		}
		return sum;
	}

	private static BigDecimal summerAvkortet(BeregningsgrunnlagPeriode grunnlag) {
        var sum = BigDecimal.ZERO;
		for (var bps : grunnlag.getBeregningsgrunnlagPrStatus()) {
			if (bps.erArbeidstakerEllerFrilanser()) {
				sum = sum.add(bps.getArbeidsforhold().stream()
						.map(BeregningsgrunnlagPrArbeidsforhold::getAndelsmessigFørGraderingPrAar)
						.reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
			} else {
				sum = sum.add(bps.getAndelsmessigFørGraderingPrAar());
			}
		}
		return sum;
	}

}
