package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.svp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
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
			if (grunnlag.getInntektgraderingsprosent() != null) {
				resultater.put("inntektgraderingsprosent", grunnlag.getInntektgraderingsprosent());
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
		var gradering = bortfalt.divide(totaltGradertGrunnlag, 10, RoundingMode.HALF_UP);
		if (gradering.compareTo(BigDecimal.ONE) < 0) {
			grunnlag.setInntektgraderingsprosent(gradering.multiply(BigDecimal.valueOf(100)));
			grenseverdi = grenseverdi.multiply(gradering);
		}
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

		return nettoBortfaltInntekt.max(BigDecimal.ZERO);
	}

	private BigDecimal finnBortfaltForStatus(BeregningsgrunnlagPrStatus bps) {
		var bortfaltInntektsprosent = finnBortfaltInntektsprosentForStatus(bps);
		var bortfaltInntektsgrad = bortfaltInntektsprosent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
		return bps.getInntektsgrunnlagPrÅr().multiply(bortfaltInntektsgrad);
	}

	/** Finner bortfalt inntektsprosent for status
	 * For statusene DP, AAP og brukers andel som brukes til ytelser og midlertidig inaktiv
	 * brukes en bortfalt inntektsgrad på 100% siden disse aktivitene ikke er løpende
	 * For andre statuser antar vi en bortfalt inntektsgrad som tilsvarer utbetalingsgraden
	 *
	 * @param bps Status
	 * @return Bortfalt inntektsprosent
	 */
	private static BigDecimal finnBortfaltInntektsprosentForStatus(BeregningsgrunnlagPrStatus bps) {
		if (bps.getAktivitetStatus().erAAPellerDP() || bps.getAktivitetStatus().equals(AktivitetStatus.BA)) {
			return BigDecimal.valueOf(100); // Regner hele grunnlaget som bortfalt ved DP/AAP/bruker andel (kun ytelse og midlertidig inaktiv)
		}
		return bps.getUtbetalingsprosent();
	}

	private BigDecimal finnBortfaltFraATFL(List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold1) {
		return arbeidsforhold1.stream()
				.map(arbeidsforhold -> {
					var bortfaltInntektsprosent = finnBortfaltInntektsprosentForArbeidsforhold(arbeidsforhold);
					var bortfaltInntektsgrad = bortfaltInntektsprosent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
					return arbeidsforhold.getInntektsgrunnlagPrÅr().multiply(bortfaltInntektsgrad);
				})
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/** Finner grad av inntekt som er bortfalt
	 * Dersom arbeidsforholdet ikke er aktivt bruker vi 100% fordi all inntekten er bortfalt
	 * Dersom arbeidsforholdet er aktivt brukes utbetalingsgraden
	 *
	 * @param arbeidsforhold Arbeidsforhold
	 * @return Bortfalt inntektsprosent
	 */
	private static BigDecimal finnBortfaltInntektsprosentForArbeidsforhold(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
		if (arbeidsforhold.getErIkkeYrkesaktiv()) {
			return BigDecimal.valueOf(100);
		}
		return arbeidsforhold.getUtbetalingsprosent();
	}
}
