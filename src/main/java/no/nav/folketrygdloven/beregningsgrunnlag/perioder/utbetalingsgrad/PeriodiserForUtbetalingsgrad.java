package no.nav.folketrygdloven.beregningsgrunnlag.perioder.utbetalingsgrad;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.AndelUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.PeriodeModellUtbetalingsgrad;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.utbetalingsgrad.PeriodiseringUtbetalingsgradProsesstruktur;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.IdentifisertePeriodeÅrsaker;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.PeriodeSplittData;
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
		List<SplittetPeriode> splittetPerioder = periodiserBeregningsgrunnlag(prosesstruktur.getInput(), prosesstruktur.getIdentifisertePeriodeÅrsaker());
		prosesstruktur.setSplittetPerioder(splittetPerioder);
		SingleEvaluation resultat = ja();
		resultat.setEvaluationProperty("splittetPerioder", splittetPerioder);
		return resultat;
	}

	private static List<SplittetPeriode> periodiserBeregningsgrunnlag(PeriodeModellUtbetalingsgrad input, IdentifisertePeriodeÅrsaker identifisertePeriodeÅrsaker) {
		// lag alle periodene, med riktige andeler
		Map<LocalDate, Set<PeriodeSplittData>> periodeMap = identifisertePeriodeÅrsaker.getPeriodeMap();

		List<Map.Entry<LocalDate, Set<PeriodeSplittData>>> entries = new ArrayList<>(periodeMap.entrySet());

		ListIterator<Map.Entry<LocalDate, Set<PeriodeSplittData>>> listIterator = entries.listIterator();

		List<SplittetPeriode> list = new ArrayList<>();
		while (listIterator.hasNext()) {
			Map.Entry<LocalDate, Set<PeriodeSplittData>> entry = listIterator.next();
			LocalDate periodeFom = entry.getKey();
			LocalDate periodeTom = utledPeriodeTom(entries, listIterator);
			Set<PeriodeSplittData> periodeSplittData = entry.getValue();

			List<SplittetAndel> nyeAndeler = input.getEndringerISøktYtelse().stream()
					.filter(utbGrad -> utbGrad.erNyAktivitetPåDato(periodeFom))
					.filter(andel -> harSøkOmUtbetalingIPeriode(andel, periodeFom) ||
							erHelgMedManuellFordelingFørOgEtter(andel, periodeFom, periodeTom) ||
							harHattRefusjonTidligereOgFortsetterYtelse(andel, input.getPeriodisertBruttoBeregningsgrunnlagList(), periodeFom))
					.map(PeriodiserForUtbetalingsgrad::mapSplittetAndel)
					.collect(Collectors.toList());

			Periode periode = new Periode(periodeFom, periodeTom);
			SplittetPeriode splittetPeriode = SplittetPeriode.builder()
					.medPeriode(periode)
					.medPeriodeÅrsaker(getPeriodeÅrsaker(periodeSplittData, input.getSkjæringstidspunkt(), periodeFom))
					.medNyeAndeler(nyeAndeler)
					.build();
			list.add(splittetPeriode);
		}
		return list;
	}

	private static boolean harHattRefusjonTidligereOgFortsetterYtelse(AndelUtbetalingsgrad gradering,
	                                                                  List<PeriodisertBruttoBeregningsgrunnlag> periodisertBruttoBeregningsgrunnlagList,
	                                                                  LocalDate periodeFom) {
		// For tilfeller der SVP har et tilkommet arbeidsforhold i SVP men det ikke søkes refusjon for dette arbeidsforholdet for alle utbetalingsperioder
		boolean harSøktYtelseIPeriode = gradering.getUbetalingsgrader() != null && gradering.getUbetalingsgrader().stream()
				.filter(uttak -> uttak.getPeriode().inneholder(periodeFom))
				.anyMatch(uttak -> uttak.getUtbetalingsprosent().compareTo(BigDecimal.ZERO) > 0);
		boolean harHattRefusjonIEnTidligerePeriode = RefusjonForUtbetalingsgradAndel.harRefusjonFørDato(gradering, periodisertBruttoBeregningsgrunnlagList, periodeFom);
		return harSøktYtelseIPeriode && harHattRefusjonIEnTidligerePeriode;
	}

	private static boolean erHelgMedManuellFordelingFørOgEtter(AndelUtbetalingsgrad andel,
	                                                           LocalDate periodeFom,
	                                                           LocalDate periodeTom) {
		// Legger til andel i periode dersom det er helg og det skal manuelt fordeles før og etter (for forenkling i gui)
		boolean skalManueltFordelesRettFør = harSøktUtbetalingOgErNyAktivitet(andel, periodeFom.minusDays(1));
		boolean skalManueltFordelesEtter = periodeTom != null && harSøktUtbetalingOgErNyAktivitet(andel, periodeTom.plusDays(1));
		boolean erHelg = erKunHelgedager(periodeFom, periodeTom);
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
		boolean harSøktUtbetaling = harSøktUtbetalingPåDato(andel, dato);
		boolean erNyAktivitet = andel.erNyAktivitetPåDato(dato);
		return harSøktUtbetaling && erNyAktivitet;
	}

	private static LocalDate utledPeriodeTom(List<Map.Entry<LocalDate, Set<PeriodeSplittData>>> entries, ListIterator<Map.Entry<LocalDate, Set<PeriodeSplittData>>> listIterator) {
		return listIterator.hasNext() ?
				entries.get(listIterator.nextIndex()).getKey().minusDays(1) :
				null;
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
		Periode ansettelsesPeriode = gradering.getArbeidsforhold() == null ? null : gradering.getArbeidsforhold().getAnsettelsesPeriode();
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

	private static List<PeriodeÅrsak> getPeriodeÅrsaker(Set<PeriodeSplittData> periodeSplittData, LocalDate skjæringstidspunkt, LocalDate periodeFom) {
		return periodeSplittData.stream()
				.map(PeriodeSplittData::getPeriodeÅrsak)
				.filter(periodeÅrsak -> !PeriodeÅrsak.UDEFINERT.equals(periodeÅrsak))
				.filter(periodeÅrsak -> !(Set.of(PeriodeÅrsak.ENDRING_I_REFUSJONSKRAV, PeriodeÅrsak.ENDRING_I_AKTIVITETER_SØKT_FOR).contains(periodeÅrsak)
						&& skjæringstidspunkt.equals(periodeFom)))
				.collect(Collectors.toList());
	}

}
