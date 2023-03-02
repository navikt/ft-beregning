package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.perioder.PeriodiserBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodiseringGraderingProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetAndel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(PeriodiserForGradering.ID)
public class PeriodiserForGradering extends LeafSpecification<PeriodiseringGraderingProsesstruktur> {

	static final String ID = FastsettPerioderGraderingRegel.ID + ".2";
	static final String BESKRIVELSE = "Periodiserer beregningsgrunnlaget ved gitte datoer og oppretter nye andeler.";

	public PeriodiserForGradering() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(PeriodiseringGraderingProsesstruktur prosesstruktur) {
		List<SplittetPeriode> splittetPerioder = periodiserBeregningsgrunnlagForGradering(prosesstruktur.getInput(), prosesstruktur.getIdentifisertePeriodeÅrsaker());
		prosesstruktur.setSplittetPerioder(splittetPerioder);
		SingleEvaluation resultat = ja();
		resultat.setEvaluationProperty("splittetPerioder", splittetPerioder);
		return resultat;
	}

	private List<SplittetPeriode> periodiserBeregningsgrunnlagForGradering(PeriodeModellGradering input, IdentifisertePeriodeÅrsaker identifisertePeriodeÅrsaker) {
		return PeriodiserBeregningsgrunnlag.periodiserBeregningsgrunnlag(new FinnNyeAndelerMedGradering(input), identifisertePeriodeÅrsaker, input.getSkjæringstidspunkt());
	}


	private static class FinnNyeAndelerMedGradering implements PeriodiserBeregningsgrunnlag.FinnNyeAndelerTjeneste {

		private final PeriodeModellGradering periodeModell;

		public FinnNyeAndelerMedGradering(PeriodeModellGradering periodeModell) {
			this.periodeModell = periodeModell;
		}

		public List<SplittetAndel> finnNyeAndeler(LocalDate periodeFom, LocalDate periodeTom) {
			return periodeModell.getAndelGraderinger().stream()
					.filter(utbGrad -> utbGrad.erNyAktivitetPåDato(periodeFom))
					.filter(andel -> harGraderingFørPeriode(andel, periodeFom))
					.map(FinnNyeAndelerMedGradering::mapSplittetAndel)
					.toList();
		}


		private static boolean harGraderingFørPeriode(AndelGradering im, LocalDate periodeFom) {
			return im.getGraderinger().stream()
					.anyMatch(gradering -> !gradering.getPeriode().getFom().isAfter(periodeFom));
		}

		private static SplittetAndel mapSplittetAndelFLSN(AndelGradering im) {
			return SplittetAndel.builder()
					.medAktivitetstatus(im.getAktivitetStatus())
					.build();
		}

		private static SplittetAndel mapSplittetAndel(AndelGradering gradering) {
			if (AktivitetStatusV2.FL.equals(gradering.getAktivitetStatus()) || AktivitetStatusV2.SN.equals(gradering.getAktivitetStatus())) {
				return mapSplittetAndelFLSN(gradering);
			}
			Periode ansettelsesPeriode = gradering.getArbeidsforhold() == null ? null : gradering.getArbeidsforhold().getAnsettelsesPeriode().orElse(null);
			SplittetAndel.Builder builder = SplittetAndel.builder()
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
