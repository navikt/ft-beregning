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
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.PleiepengerSyktBarnGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.TilkommetInntekt;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.StandardCombinators;

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

		//gradering mot uttak
		BigDecimal summerAvkortetGradertMotUttak = summerAvkortetGradertMotUttak(grunnlag);
		var grenseverdi = summerAvkortetGradertMotUttak;
		BigDecimal sumAvkortet = summerAvkortet(grunnlag);
		BigDecimal totalUtbetalingsgradFraUttak = sumAvkortet.signum() != 0 ? summerAvkortetGradertMotUttak.divide(sumAvkortet, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
		grunnlag.setTotalUtbetalingsgradFraUttak(totalUtbetalingsgradFraUttak);
		resultater.put("totalUtbetalingsgradFraUttak", totalUtbetalingsgradFraUttak);

		grenseverdi = utførMidlertidigInaktivGradering(grunnlag, resultater, grenseverdi);
		grenseverdi = utførYtelsesspesifikkGradering(grunnlag, grenseverdi);
		grenseverdi = utførGraderingMotInntekt(grunnlag, resultater, grenseverdi, totalUtbetalingsgradFraUttak);

		resultater.put("grenseverdi", grenseverdi);
		grunnlag.setGrenseverdi(grenseverdi);
		SingleEvaluation resultat = ja();
		resultat.setEvaluationProperties(resultater);
		return resultat;

	}

	private BigDecimal utførGraderingMotInntekt(BeregningsgrunnlagPeriode grunnlag, Map<String, Object> resultater, BigDecimal grenseverdi, BigDecimal totalUtbetalingsgradFraUttak) {
		//juster ned med tilkommet inntekt hvis det gir lavere utbetaling enn overstående
		if (grunnlag.getBeregningsgrunnlag().getToggles().isEnabled("GRADERING_MOT_INNTEKT", false) && !grunnlag.getTilkommetInntektsforholdListe().isEmpty()) {
			BigDecimal graderingPåToppenAvUttakgraderingPgaTilkommetInntekt = andelBeholdtEtterGradertMotTilkommetInntekt(grunnlag);
			resultater.put("graderingPåToppenAvUttakgraderingPgaTilkommetInntekt", min(BigDecimal.ONE, graderingPåToppenAvUttakgraderingPgaTilkommetInntekt));
			BigDecimal totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt = min(BigDecimal.ONE, totalUtbetalingsgradFraUttak.multiply(graderingPåToppenAvUttakgraderingPgaTilkommetInntekt));
			resultater.put("totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt", totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt);
			grunnlag.setTotalUtbetalingsgradEtterReduksjonVedTilkommetInntekt(totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt);

			//deprecated etter totalUtbetalingsgradEtterReduksjonVedTilkommetInntekt ble lagt til?
			grenseverdi = graderingPåToppenAvUttakgraderingPgaTilkommetInntekt.compareTo(BigDecimal.ONE) < 0 ? grenseverdi.multiply(graderingPåToppenAvUttakgraderingPgaTilkommetInntekt) : grenseverdi;
			if (grunnlag.getInntektsgraderingFraBruttoBeregningsgrunnlag() != null) {
				resultater.put("inntektgraderingsprosent", grunnlag.getInntektsgraderingFraBruttoBeregningsgrunnlag());
			}
		}
		return grenseverdi;
	}

	private static BigDecimal utførMidlertidigInaktivGradering(BeregningsgrunnlagPeriode grunnlag, Map<String, Object> resultater, BigDecimal grenseverdi) {
		//hvis §8-47a, skaler med fast faktor
		var erInaktivTypeA = MidlertidigInaktivType.A.equals(grunnlag.getBeregningsgrunnlag().getMidlertidigInaktivType());
		if (erInaktivTypeA) {
			BigDecimal reduksjonsfaktor = grunnlag.getBeregningsgrunnlag().getMidlertidigInaktivTypeAReduksjonsfaktor();
			grenseverdi = grenseverdi.multiply(reduksjonsfaktor);
			resultater.put("grad847a", reduksjonsfaktor);
		}
		return grenseverdi;
	}

	private static BigDecimal utførYtelsesspesifikkGradering(BeregningsgrunnlagPeriode grunnlag, BigDecimal grenseverdi) {
		if (grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag() != null && grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag() instanceof PleiepengerSyktBarnGrunnlag psbGrunnlag) {
			var tilsynsgraderingsprosent = psbGrunnlag.getTilsynsgraderingsprosent().compress((b1, b2) -> b1.compareTo(b2) == 0, StandardCombinators::leftOnly);
			var intersection = tilsynsgraderingsprosent.intersection(new LocalDateInterval(grunnlag.getPeriodeFom(), grunnlag.getPeriodeTom()));
			if (intersection.toSegments().size() > 1) {
				throw new IllegalStateException("Fant flere tilsynsgrader for samme periode " + grunnlag.getPeriodeFom());
			}
			if (!intersection.isEmpty()) {
				var tilsynsgradering = intersection.toSegments().first().getValue();
				grenseverdi = grenseverdi.multiply(tilsynsgradering.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
			}
		}
		return grenseverdi;
	}


	static BigDecimal min(BigDecimal a, BigDecimal b) {
		return a.compareTo(b) > 0 ? b : a;
	}

	private static BigDecimal summerAvkortetGradertMotUttak(BeregningsgrunnlagPeriode grunnlag) {
		BigDecimal sum = BigDecimal.ZERO;
		for (BeregningsgrunnlagPrStatus bps : grunnlag.getBeregningsgrunnlagPrStatusSomSkalBrukes()) {
			if (bps.erArbeidstakerEllerFrilanser()) {
				sum = sum.add(bps.getArbeidsforholdSomSkalBrukes().stream()
						.map(arb -> arb.getAndelsmessigFørGraderingPrAar().multiply(arb.getUtbetalingsprosent().scaleByPowerOfTen(-2)))
						.reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
			} else {
				sum = sum.add(bps.getAndelsmessigFørGraderingPrAar().multiply(bps.getUtbetalingsprosent().scaleByPowerOfTen(-2)));
			}
		}
		return sum;
	}

	private static BigDecimal summerAvkortet(BeregningsgrunnlagPeriode grunnlag) {
		BigDecimal sum = BigDecimal.ZERO;
		for (BeregningsgrunnlagPrStatus bps : grunnlag.getBeregningsgrunnlagPrStatusSomSkalBrukes()) {
			if (bps.erArbeidstakerEllerFrilanser()) {
				sum = sum.add(bps.getArbeidsforholdSomSkalBrukes().stream()
						.map(BeregningsgrunnlagPrArbeidsforhold::getAndelsmessigFørGraderingPrAar)
						.reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
			} else {
				sum = sum.add(bps.getAndelsmessigFørGraderingPrAar());
			}
		}
		return sum;
	}

	private BigDecimal andelBeholdtEtterGradertMotTilkommetInntekt(BeregningsgrunnlagPeriode grunnlag) {
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

		return bortfalt.divide(totaltGradertGrunnlag, 10, RoundingMode.HALF_UP);
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
