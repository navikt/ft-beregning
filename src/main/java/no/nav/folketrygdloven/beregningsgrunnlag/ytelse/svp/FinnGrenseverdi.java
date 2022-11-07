package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
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

	public static final String ID = "FP_BR 6.2";
	public static final String BESKRIVELSE = "Finn grenseverdi";

	public FinnGrenseverdi() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		Map<String, Object> resultater = new HashMap<>();
		BigDecimal sumAvkortetSkalBrukes = BigDecimal.ZERO;
		for (BeregningsgrunnlagPrStatus bps : grunnlag.getBeregningsgrunnlagPrStatusSomSkalBrukes()) {
			if (bps.erArbeidstakerEllerFrilanser()) {
				sumAvkortetSkalBrukes = sumAvkortetSkalBrukes.add(bps.getArbeidsforholdSomSkalBrukes().stream()
						.map(arb -> arb.getAndelsmessigFørGraderingPrAar().multiply(arb.getUtbetalingsprosent().scaleByPowerOfTen(-2)))
						.reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
			} else {
				sumAvkortetSkalBrukes = sumAvkortetSkalBrukes.add(bps.getAndelsmessigFørGraderingPrAar().multiply(bps.getUtbetalingsprosent().scaleByPowerOfTen(-2)));
			}
		}
		var erInaktivTypeA = MidlertidigInaktivType.A.equals(grunnlag.getBeregningsgrunnlag().getMidlertidigInaktivType());
		var grenseverdi = sumAvkortetSkalBrukes;
		if (erInaktivTypeA) {
			grenseverdi = grenseverdi.multiply(grunnlag.getBeregningsgrunnlag().getMidlertidigInaktivTypeAReduksjonsfaktor());
		}
		if (grunnlag.getBeregningsgrunnlag().getToggles().isEnabled("GRADERING_MOT_INNTEKT", false)) {
			grenseverdi = gradertMotTilkommetInntekt(grunnlag, grenseverdi);
		}
		resultater.put("grenseverdi", grenseverdi);
		grunnlag.setGrenseverdi(grenseverdi);
		SingleEvaluation resultat = ja();
		resultat.setEvaluationProperties(resultater);
		return resultat;

	}

	private BigDecimal gradertMotTilkommetInntekt(BeregningsgrunnlagPeriode grunnlag, BigDecimal grenseverdi) {
		BigDecimal bortfalt = finnBortfaltInntekt(grunnlag);
		var totaltGradertGrunnlag = grunnlag.getBeregningsgrunnlagPrStatus().stream()
				.map(BeregningsgrunnlagPrStatus::getGradertBeregnetPrÅr)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		var gradering = bortfalt.divide(totaltGradertGrunnlag, 10, RoundingMode.HALF_UP);
		grenseverdi = grenseverdi.multiply(gradering);
		return grenseverdi;
	}

	private BigDecimal finnBortfaltInntekt(BeregningsgrunnlagPeriode grunnlag) {
		var bortfalt = BigDecimal.ZERO;
		for (BeregningsgrunnlagPrStatus bps : grunnlag.getBeregningsgrunnlagPrStatusSomSkalBrukes()) {
			if (bps.erArbeidstakerEllerFrilanser()) {
				bortfalt = bortfalt.add(finnBortfaltFraATFL(bps.getArbeidsforhold()));
			} else {
				bortfalt = bortfalt.add(finnBortfaltForStatus(bps));
			}
		}
		return bortfalt;
	}

	private BigDecimal finnBortfaltForStatus(BeregningsgrunnlagPrStatus bps) {
		var utbetalingsprosent = bps.getUtbetalingsprosent();
		var utbetalingsgrad = utbetalingsprosent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
		var inversUtbetalingsgrad = BigDecimal.ONE.subtract(utbetalingsgrad);
		var løpendeInntekt = bps.getBeregnetPrÅr().multiply(inversUtbetalingsgrad)
				.add(bps.getTilkommetPrÅr());
		return bps.getBruttoInkludertNaturalytelsePrÅr().subtract(løpendeInntekt);
	}

	private BigDecimal finnBortfaltFraATFL(List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold1) {
		return arbeidsforhold1.stream()
				.map(arbeidsforhold -> {
					var utbetalingsprosent = arbeidsforhold.getUtbetalingsprosent();
					var utbetalingsgrad = utbetalingsprosent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
					var inversUtbetalingsgrad = BigDecimal.ONE.subtract(utbetalingsgrad);
					var opprettholdtInntekt = arbeidsforhold.getBeregnetPrÅr()
							.multiply(inversUtbetalingsgrad)
							.add(arbeidsforhold.getTilkommetPrÅr());
					return arbeidsforhold.getBruttoInkludertNaturalytelsePrÅr().orElse(BigDecimal.ZERO)
							.subtract(opprettholdtInntekt);
				})
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
