package no.nav.folketrygdloven.beregningsgrunnlag.grenseverdi.utenfordeling;

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
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FinnGrenseverdiUtenFordeling.ID)
class FinnGrenseverdiUtenFordeling extends LeafSpecification<BeregningsgrunnlagPeriode> {

	public static final String ID = "FP_BR 6.2_uten_fordeling";
	public static final String BESKRIVELSE = "Finn grenseverdi uten fordeling";

	public FinnGrenseverdiUtenFordeling() {
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


		//juster ned med tilkommet inntekt hvis det gir lavere utbetaling enn overstående
		BigDecimal totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt = null;
		if (!grunnlag.getTilkommetInntektsforholdListe().isEmpty()) {
            var graderingPåToppenAvUttakgraderingPgaTilkommetInntekt = andelBeholdtEtterGradertMotTilkommetInntekt(grunnlag);
			resultater.put("graderingPåToppenAvUttakgraderingPgaTilkommetInntekt", min(BigDecimal.ONE, graderingPåToppenAvUttakgraderingPgaTilkommetInntekt));
			totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt = min(BigDecimal.ONE, totalUtbetalingsgradFraUttak.multiply(graderingPåToppenAvUttakgraderingPgaTilkommetInntekt));
			resultater.put("totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt", totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt);
			grunnlag.setTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt(totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt);

			//deprecated etter totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt ble lagt til?
			grenseverdi = graderingPåToppenAvUttakgraderingPgaTilkommetInntekt.compareTo(BigDecimal.ONE) < 0 ? grenseverdi.multiply(graderingPåToppenAvUttakgraderingPgaTilkommetInntekt) : grenseverdi;
			if (grunnlag.getInntektsgraderingFraBruttoBeregningsgrunnlag() != null) {
				resultater.put("inntektgraderingsprosent", grunnlag.getInntektsgraderingFraBruttoBeregningsgrunnlag());
			}
		}

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

			if (totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt != null) {
                var justertTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt = totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt.multiply(reduksjonsfaktor);
				grunnlag.setTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt(justertTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt);
				resultater.put("totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt", justertTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt);
			}
		}

		resultater.put("grenseverdi", grenseverdi);
		grunnlag.setGrenseverdi(grenseverdi);
        var resultat = ja();
		resultat.setEvaluationProperties(resultater);
		return resultat;
	}

	static BigDecimal min(BigDecimal a, BigDecimal b){
		return a.compareTo(b) > 0 ? b : a;
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

	private BigDecimal andelBeholdtEtterGradertMotTilkommetInntekt(BeregningsgrunnlagPeriode grunnlag) {
        var bortfalt = finnBortfaltInntekt(grunnlag);
		var totaltGradertGrunnlag = grunnlag.getBeregningsgrunnlagPrStatus().stream()
				.map(BeregningsgrunnlagPrStatus::getGradertInntektsgrunnlagInkludertNaturalytelsePrÅr)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		if (totaltGradertGrunnlag.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		var totaltGrunnlag = grunnlag.getBeregningsgrunnlagPrStatus().stream()
				.map(BeregningsgrunnlagPrStatus::getInntektsgrunnlagInkludertNaturalytelsePrÅr)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		var graderingMotTotal = bortfalt.divide(totaltGrunnlag, 10, RoundingMode.HALF_UP);
		grunnlag.setInntektsgraderingFraBruttoBeregningsgrunnlag(graderingMotTotal.multiply(BigDecimal.valueOf(100)));

		return bortfalt.divide(totaltGradertGrunnlag, 10, RoundingMode.HALF_UP);
	}

	private BigDecimal finnBortfaltInntekt(BeregningsgrunnlagPeriode grunnlag) {
		var bortfalt = BigDecimal.ZERO;
		for (var bps : grunnlag.getBeregningsgrunnlagPrStatus()) {
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

		var bruttoBortfalt = bortfalt.subtract(tilkommetInntekt);
		return bruttoBortfalt.max(BigDecimal.ZERO);
	}

	private BigDecimal finnBortfaltForStatus(BeregningsgrunnlagPrStatus bps) {
		var aktivitetsgradOpt = bps.getAktivitetsgrad();
		if (aktivitetsgradOpt.isPresent()) {
			var aktivitetsgrad = aktivitetsgradOpt.get().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
			var opprettholdtInntekt = bps.getInntektsgrunnlagPrÅr().multiply(aktivitetsgrad);
			return bps.getInntektsgrunnlagPrÅr().subtract(opprettholdtInntekt);
		}
		var utbetalingsprosent = bps.getUtbetalingsprosent();
		var utbetalingsgrad = utbetalingsprosent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
		return bps.getInntektsgrunnlagPrÅr().multiply(utbetalingsgrad);
	}

	private BigDecimal finnBortfaltFraATFL(List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforhold1) {
		return arbeidsforhold1.stream()
				.map(arbeidsforhold -> {
					var aktivitetsgradOpt = arbeidsforhold.getAktivitetsgrad();
					if (aktivitetsgradOpt.isPresent()) {
						var aktivitetsgrad = aktivitetsgradOpt.get().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
						var opprettholdtInntekt = arbeidsforhold.getInntektsgrunnlagPrÅr().multiply(aktivitetsgrad);
						return arbeidsforhold.getInntektsgrunnlagPrÅr().subtract(opprettholdtInntekt);
					}
					var utbetalingsprosent = arbeidsforhold.getUtbetalingsprosent();
					var utbetalingsgrad = utbetalingsprosent.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
					return arbeidsforhold.getInntektsgrunnlagPrÅr().multiply(utbetalingsgrad);
				})
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
