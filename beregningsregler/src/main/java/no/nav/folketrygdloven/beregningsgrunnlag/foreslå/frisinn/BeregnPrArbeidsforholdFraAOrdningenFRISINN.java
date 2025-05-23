package no.nav.folketrygdloven.beregningsgrunnlag.foreslå.frisinn;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.frisinn.FrisinnGrunnlag;
import no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn.FinnPerioderUtenYtelse;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnPrArbeidsforholdFraAOrdningenFRISINN.ID)
class BeregnPrArbeidsforholdFraAOrdningenFRISINN extends LeafSpecification<BeregningsgrunnlagPeriode> {
	private static final BigDecimal ANTALL_MÅNEDER_I_ÅR = BigDecimal.valueOf(12);
	private static final LocalDate NYOPPSTARTET_FL_GRENSE = LocalDate.of(2019, 3, 1);
	static final String ID = "FRISINN 2.3";
	static final String BESKRIVELSE = "Rapportert inntekt = snitt av mnd-inntekter i beregningsperioden * 12";
	private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

	BeregnPrArbeidsforholdFraAOrdningenFRISINN(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
		super(ID, BESKRIVELSE);
		Objects.requireNonNull(arbeidsforhold, "arbeidsforhold");
		this.arbeidsforhold = arbeidsforhold;
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		Map<String, Object> resultater = new HashMap<>();
		if (Boolean.TRUE.equals(arbeidsforhold.getFastsattAvSaksbehandler())) {
			resultater.put("beregnetPrÅr", arbeidsforhold.getBeregnetPrÅr());
			return beregnet(resultater);
		}
        var inntektsgrunnlag = grunnlag.getInntektsgrunnlag();
        var ytelsesSpesifiktGrunnlag = grunnlag.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag();
		if (!(ytelsesSpesifiktGrunnlag instanceof FrisinnGrunnlag frisinnGrunnlag)) {
			throw new IllegalStateException("Har ikke frisinngrunnlag for fastsetting av frilans, ugyldig tilstand");
		}
        var skjæringstidspunktOpptjening = frisinnGrunnlag.getSkjæringstidspunktOpptjening();
        var perioderSomSkalBrukesForInntekter = FinnPerioderUtenYtelse.finnPerioder(inntektsgrunnlag, skjæringstidspunktOpptjening);

		resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
		BigDecimal årsinntekt;
		if (arbeidsforhold.erFrilanser()) {
			if (frisinnGrunnlag.søkerYtelseFrilans() && finnesIkkeInntektForFLFørFristOgFinnesEtterFrist(grunnlag, skjæringstidspunktOpptjening)) {
				// Beregnes som nyoppstartet fl
				perioderSomSkalBrukesForInntekter = lagMånederUtenYtelseEtterFørsteInntektsdag(grunnlag, perioderSomSkalBrukesForInntekter, skjæringstidspunktOpptjening);
			} else if (perioderSomSkalBrukesForInntekter.isEmpty()) {
				perioderSomSkalBrukesForInntekter = lag12MånederFørOgInkludertDato(skjæringstidspunktOpptjening.minusMonths(36), skjæringstidspunktOpptjening.minusMonths(1));
			}
			// Hvis det ikke søkes ytelse for frilans skal kun oppgitt inntekt legges til grunn. Setter registerinntekt på første periode for vilkårssjekk FRILANS_UTEN_INNTEKT
			årsinntekt = frisinnGrunnlag.søkerFrilansISøknadsperiode(grunnlag.getPeriodeFom()) || erFørstePeriodeOgSøktFrilansIMinstEnPeriode(grunnlag, frisinnGrunnlag)
					? beregnÅrsinntektFrilans(perioderSomSkalBrukesForInntekter, inntektsgrunnlag, grunnlag, resultater)
					: finnOppgittÅrsinntektFL(inntektsgrunnlag, grunnlag)
					.orElse(BigDecimal.ZERO);
		} else {
			årsinntekt = beregnÅrsinntektArbeidstaker(perioderSomSkalBrukesForInntekter, inntektsgrunnlag, grunnlag, resultater);
		}

		BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
				.medBeregnetPrÅr(årsinntekt)
				.build();
		resultater.put("antallPerioder", perioderSomSkalBrukesForInntekter.size());
		resultater.put("beregnetPrÅr", årsinntekt);
		return beregnet(resultater);
	}

	private boolean erFørstePeriodeOgSøktFrilansIMinstEnPeriode(BeregningsgrunnlagPeriode grunnlag, FrisinnGrunnlag frisinnGrunnlag) {
		return grunnlag.getPeriodeFom().isEqual(grunnlag.getSkjæringstidspunkt()) && frisinnGrunnlag.søkerYtelseFrilans();
	}

	private boolean finnesIkkeInntektForFLFørFristOgFinnesEtterFrist(BeregningsgrunnlagPeriode grunnlag, LocalDate skjæringstidspunktOpptjening) {
        var startAvPeriodeSomDefinererAktiveFrilansere = LocalDate.of(2018, 1, 1);
        var periodeFørFrist = Periode.of(startAvPeriodeSomDefinererAktiveFrilansere, NYOPPSTARTET_FL_GRENSE.minusDays(1));
        var periodeEtterFrist = Periode.of(NYOPPSTARTET_FL_GRENSE, skjæringstidspunktOpptjening);
        var frilansinntekterFørNyoppstartetGrense = grunnlag.getInntektsgrunnlag().finnAlleFrilansInntektPerioder(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, periodeFørFrist);
        var frilansinntekterEtterNyoppstartetGrense = grunnlag.getInntektsgrunnlag().finnAlleFrilansInntektPerioder(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, periodeEtterFrist);
		return frilansinntekterFørNyoppstartetGrense.isEmpty() && !frilansinntekterEtterNyoppstartetGrense.isEmpty();
	}

	private List<Periode> lagMånederUtenYtelseEtterFørsteInntektsdag(BeregningsgrunnlagPeriode grunnlag,
	                                                                 List<Periode> perioderUtenYtelse,
	                                                                 LocalDate skjæringstidspunktOpptjening) {
        var perioderEtterGrenseUtenYtelse = perioderUtenYtelse.stream()
				.filter(p -> !p.getFom().isBefore(NYOPPSTARTET_FL_GRENSE))
				.toList();
        var førsteDatoMedInntekt = finnFørsteDatoMedFrilansInntektEtterDato(grunnlag, NYOPPSTARTET_FL_GRENSE, skjæringstidspunktOpptjening);
		if (perioderEtterGrenseUtenYtelse.isEmpty()) {
			// Lager 12 måneder før første inntektsdato
			return lag12MånederFørOgInkludertDato(førsteDatoMedInntekt, skjæringstidspunktOpptjening.minusMonths(1));
		}
		return perioderEtterGrenseUtenYtelse.stream()
				.filter(p -> !p.getFom().isBefore(førsteDatoMedInntekt))
				.toList();
	}

	private List<Periode> lag12MånederFørOgInkludertDato(LocalDate tidligsteDato, LocalDate senesteDato) {
        var current = senesteDato.withDayOfMonth(1);
		List<Periode> perioder = new ArrayList<>();
		while (current.isAfter(senesteDato.minusMonths(12)) && !current.isBefore(tidligsteDato)) {
			perioder.add(Periode.of(current, current.with(TemporalAdjusters.lastDayOfMonth())));
			current = current.minusMonths(1);
		}
		return perioder;
	}

	private LocalDate finnFørsteDatoMedFrilansInntektEtterDato(BeregningsgrunnlagPeriode grunnlag, LocalDate nyoppstartetGrense, LocalDate skjæringstidspunktOpptjening) {
        var periode = Periode.of(nyoppstartetGrense, skjæringstidspunktOpptjening);
        var frilansInntekter = grunnlag.getBeregningsgrunnlag().getInntektsgrunnlag()
				.finnAlleFrilansInntektPerioder(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, periode);
		return frilansInntekter.stream()
				.map(Periodeinntekt::getFom)
				.min(Comparator.naturalOrder())
				.orElseThrow(() -> new IllegalStateException("Skal ha inntekt i periode for nyoppstartet"));
	}

	private BigDecimal beregnÅrsinntektFrilans(List<Periode> inntektsperioder, Inntektsgrunnlag inntektsgrunnlag, BeregningsgrunnlagPeriode grunnlag, Map<String, Object> resultater) {
        var samletInntekt = BigDecimal.ZERO;
		for (var periode : inntektsperioder) {
			samletInntekt = samletInntekt.add(finnInntektForPeriode(periode, inntektsgrunnlag, resultater).orElse(BigDecimal.ZERO));
		}
        var antallPerioder = BigDecimal.valueOf(inntektsperioder.size());
        var snittMånedslønnFraRegister = inntektsperioder.isEmpty() ? BigDecimal.ZERO : samletInntekt.divide(antallPerioder, 10, RoundingMode.HALF_EVEN);
        var årslønnFraRegister = snittMånedslønnFraRegister.multiply(ANTALL_MÅNEDER_I_ÅR);
		resultater.put("årsinntektFraRegister", snittMånedslønnFraRegister);
		if (årslønnFraRegister.compareTo(BigDecimal.ZERO) > 0) {
            var årsinntektFraSøknad = finnOppgittÅrsinntektFL(inntektsgrunnlag, grunnlag).orElse(BigDecimal.ZERO);
			resultater.put("årsinntektFraSøknad", årsinntektFraSøknad);
			return årslønnFraRegister.max(årsinntektFraSøknad);
		}
		return årslønnFraRegister;
	}

	private BigDecimal beregnÅrsinntektArbeidstaker(List<Periode> inntektsperioder, Inntektsgrunnlag inntektsgrunnlag, BeregningsgrunnlagPeriode grunnlag, Map<String, Object> resultater) {
        var antallPerioderMedInntekt = inntektsperioder.size();
		if (antallPerioderMedInntekt == 0) {
            var månedslønn = inntektsgrunnlag.getPeriodeinntekter(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, grunnlag.getBeregningsgrunnlagPeriode().getFom(), 1).stream()
					.map(Periodeinntekt::getInntekt)
					.reduce(BigDecimal::add)
					.orElse(BigDecimal.ZERO);
			return månedslønn.multiply(ANTALL_MÅNEDER_I_ÅR);
		}
        var samletInntekt = inntektsperioder.stream()
				.map(p -> finnInntektForPeriode(p, inntektsgrunnlag, resultater))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
		resultater.put("perioderMedInntekter ", antallPerioderMedInntekt);
        var antallPerioder = BigDecimal.valueOf(antallPerioderMedInntekt);
        var snittMånedslønn = samletInntekt.divide(antallPerioder, 10, RoundingMode.HALF_EVEN);
		return snittMånedslønn.multiply(ANTALL_MÅNEDER_I_ÅR);
	}

	private Optional<BigDecimal> finnInntektForPeriode(Periode periode, Inntektsgrunnlag inntektsgrunnlag, Map<String, Object> resultater) {
        var inntekterHosAgForPeriode = inntektsgrunnlag.getInntektForArbeidsforholdIPeriode(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, periode);
		if (inntekterHosAgForPeriode.isEmpty()) {
			return Optional.empty();
		}
        var sumForPeriode = inntekterHosAgForPeriode.stream().map(Periodeinntekt::getInntekt).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
		resultater.put("sumForPeriode" + periode.toString(), sumForPeriode);
		return Optional.of(sumForPeriode);

	}

	private Optional<BigDecimal> finnOppgittÅrsinntektFL(Inntektsgrunnlag inntektsgrunnlag, BeregningsgrunnlagPeriode grunnlag) {
		return inntektsgrunnlag.getOppgittInntektForStatusIPeriode(AktivitetStatus.FL, grunnlag.getBeregningsgrunnlagPeriode());
	}
}
