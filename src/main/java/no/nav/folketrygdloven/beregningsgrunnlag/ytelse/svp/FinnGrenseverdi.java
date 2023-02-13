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
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.TilkommetInntekt;
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
		if (grunnlag.getBeregningsgrunnlag().getToggles().isEnabled("GRADERING_MOT_INNTEKT", false) && !grunnlag.getTilkommetInntektsforholdListe().isEmpty()) {
			grenseverdi = gradertMotTilkommetInntekt(grunnlag, grenseverdi);
			if (grunnlag.getInntektsgraderingFraBruttoBeregningsgrunnlag() != null) {
				resultater.put("inntektgraderingsprosent", grunnlag.getInntektsgraderingFraBruttoBeregningsgrunnlag());
			}
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
				.map(BeregningsgrunnlagPrStatus::getGradertBruttoInkludertNaturalytelsePrÅr)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		if (totaltGradertGrunnlag.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		var totaltGrunnlag = grunnlag.getBeregningsgrunnlagPrStatus().stream()
				.map(BeregningsgrunnlagPrStatus::getBruttoInkludertNaturalytelsePrÅr)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		var graderingMotTotal = bortfalt.divide(totaltGrunnlag, 10, RoundingMode.HALF_UP);
		grunnlag.setInntektsgraderingFraBruttoBeregningsgrunnlag(graderingMotTotal.multiply(BigDecimal.valueOf(100)));

		// Grenseverdien er allerede gradert mot arbeidstid her
		var graderingEtterGraderingMotArbeidstid = bortfalt.divide(totaltGradertGrunnlag, 10, RoundingMode.HALF_UP);
		grenseverdi = grenseverdi.multiply(graderingEtterGraderingMotArbeidstid);
		return grenseverdi;
	}

	private BigDecimal finnBortfaltInntekt(BeregningsgrunnlagPeriode grunnlag) {
		var bortfalt = BigDecimal.ZERO;
		for (BeregningsgrunnlagPrStatus bps : grunnlag.getBeregningsgrunnlagPrStatus()) {
			if (bps.erArbeidstakerEllerFrilanser()) {
				bortfalt = bortfalt.add(finnBortfaltFraATFL(bps.getArbeidsforhold()));
			} else {
				bortfalt = bortfalt.add(finnBortfaltForStatus(bps));
			}
		}

		var tilkommetInntekt = grunnlag.getTilkommetInntektsforholdListe().stream()
				.map(TilkommetInntekt::getTilkommetPrÅr)
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);

		var nettoBortfaltInntekt = bortfalt.subtract(tilkommetInntekt);

		return nettoBortfaltInntekt;
	}

	private BigDecimal finnBortfaltForStatus(BeregningsgrunnlagPrStatus bps) {
		var utbetalingsprosent = bps.getUtbetalingsprosent();
		var utbetalingsgrad = utbetalingsprosent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
		return bps.getInntektsgrunnlagPrÅr().multiply(utbetalingsgrad);
	}

	private BigDecimal finnBortfaltFraATFL(List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold1) {
		return arbeidsforhold1.stream()
				.map(arbeidsforhold -> {
					var utbetalingsprosent = arbeidsforhold.getUtbetalingsprosent();
					var utbetalingsgrad = utbetalingsprosent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
					return arbeidsforhold.getInntektsgrunnlagPrÅr().multiply(utbetalingsgrad);
				})
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
