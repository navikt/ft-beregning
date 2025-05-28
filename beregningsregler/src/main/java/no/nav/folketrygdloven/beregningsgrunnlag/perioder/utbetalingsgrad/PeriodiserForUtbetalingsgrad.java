package no.nav.folketrygdloven.beregningsgrunnlag.perioder.utbetalingsgrad;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.perioder.PeriodiserBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.AndelUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.PeriodeModellUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.PeriodiseringUtbetalingsgradProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(PeriodiserForUtbetalingsgrad.ID)
public class PeriodiserForUtbetalingsgrad extends LeafSpecification<PeriodiseringUtbetalingsgradProsesstruktur> {

	static final String ID = FastsettPerioderForUtbetalingsgradRegel.ID + ".2";
	static final String BESKRIVELSE = "Periodiserer beregningsgrunnlaget ved gitte datoer og oppretter nye andeler.";

	public PeriodiserForUtbetalingsgrad() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(PeriodiseringUtbetalingsgradProsesstruktur prosesstruktur) {
        var splittetPerioder = periodiserBeregningsgrunnlagForUtbetalingsgrad(prosesstruktur.getInput(), prosesstruktur.getIdentifisertePeriodeÅrsaker());
		prosesstruktur.setSplittetPerioder(splittetPerioder);
        var resultat = ja();
		resultat.setEvaluationProperty("splittetPerioder", splittetPerioder);
		return resultat;
	}

	private List<SplittetPeriode> periodiserBeregningsgrunnlagForUtbetalingsgrad(PeriodeModellUtbetalingsgrad input, IdentifisertePeriodeÅrsaker identifisertePeriodeÅrsaker) {
		return PeriodiserBeregningsgrunnlag.periodiserBeregningsgrunnlag(new FinnNyeAndelerMedUtbetalingsgrad(input), identifisertePeriodeÅrsaker, input.getSkjæringstidspunkt());
	}


	private static class FinnNyeAndelerMedUtbetalingsgrad implements PeriodiserBeregningsgrunnlag.FinnNyeAndelerTjeneste {

		private final PeriodeModellUtbetalingsgrad periodeModell;

		public FinnNyeAndelerMedUtbetalingsgrad(PeriodeModellUtbetalingsgrad periodeModell) {
			this.periodeModell = periodeModell;
		}

		public List<SplittetAndel> finnNyeAndeler(LocalDate periodeFom, LocalDate periodeTom) {
			return periodeModell.getEndringerISøktYtelse().stream()
					.filter(utbGrad -> utbGrad.erNyAktivitetPåDato(periodeFom))
					.filter(andel -> harSøkOmUtbetalingIPeriode(andel, periodeFom) ||
							erHelgMedManuellFordelingFørOgEtter(andel, periodeFom, periodeTom))
					.map(FinnNyeAndelerMedUtbetalingsgrad::mapSplittetAndel)
					.toList();
		}

		private static boolean erHelgMedManuellFordelingFørOgEtter(AndelUtbetalingsgrad andel,
		                                                           LocalDate periodeFom,
		                                                           LocalDate periodeTom) {
			// Legger til andel i periode dersom det er helg og det skal manuelt fordeles før og etter (for forenkling i gui)
            var skalManueltFordelesRettFør = harSøktUtbetalingOgErNyAktivitet(andel, periodeFom.minusDays(1));
            var skalManueltFordelesEtter = periodeTom != null && harSøktUtbetalingOgErNyAktivitet(andel, periodeTom.plusDays(1));
            var erHelg = erKunHelgedager(periodeFom, periodeTom);
			return erHelg && (skalManueltFordelesRettFør && skalManueltFordelesEtter);
		}

		private static boolean erKunHelgedager(LocalDate fom, LocalDate tom) {
			return fom.getDayOfWeek().equals(DayOfWeek.SATURDAY) && fom.plusDays(1).equals(tom);
		}

		private static boolean harSøkOmUtbetalingIPeriode(AndelUtbetalingsgrad andel, LocalDate periodeFom) {
			return harSøktUtbetalingPåDato(andel, periodeFom);
		}

		private static boolean harSøktUtbetalingPåDato(AndelUtbetalingsgrad andel, LocalDate periodeFom) {
			return andel.getUbetalingsgrader().stream()
					.anyMatch(g -> g.getPeriode().inneholder(periodeFom) &&
							g.getUtbetalingsprosent().compareTo(BigDecimal.ZERO) > 0);
		}

		private static boolean harSøktUtbetalingOgErNyAktivitet(AndelUtbetalingsgrad andel,
		                                                        LocalDate dato) {
            var harSøktUtbetaling = harSøktUtbetalingPåDato(andel, dato);
            var erNyAktivitet = andel.erNyAktivitetPåDato(dato);
			return harSøktUtbetaling && erNyAktivitet;
		}

		private static SplittetAndel mapSplittetAndelFLSN(AndelUtbetalingsgrad im) {
			return SplittetAndel.builder()
					.medAktivitetstatus(im.getAktivitetStatus())
					.build();
		}

		private static SplittetAndel mapSplittetAndel(AndelUtbetalingsgrad gradering) {
			if (AktivitetStatusV2.FL.equals(gradering.getAktivitetStatus()) || AktivitetStatusV2.SN.equals(gradering.getAktivitetStatus())) {
				return mapSplittetAndelFLSN(gradering);
			}
            var ansettelsesPeriode = gradering.getArbeidsforhold() == null ? null : gradering.getArbeidsforhold().getAnsettelsesPeriode().orElse(null);
            var builder = SplittetAndel.builder()
					.medAktivitetstatus(gradering.getAktivitetStatus())
					.medArbeidsforhold(gradering.getArbeidsforhold());
			settAnsettelsesPeriodeHvisFinnes(ansettelsesPeriode, builder);
			return builder.build();
		}

		private static void settAnsettelsesPeriodeHvisFinnes(Periode ansettelsesPeriode, SplittetAndel.Builder builder) {
			if (ansettelsesPeriode != null) {
				builder
						.medArbeidsperiodeFom(ansettelsesPeriode.getFom())
						.medArbeidsperiodeTom(ansettelsesPeriode.getTom());
			}
		}


	}

}
