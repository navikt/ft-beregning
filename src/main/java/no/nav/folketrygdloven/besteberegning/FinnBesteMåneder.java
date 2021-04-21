package no.nav.folketrygdloven.besteberegning;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.RelatertYtelseType;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.FinnGjennomsnittligPGI;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;
import no.nav.folketrygdloven.besteberegning.modell.BesteberegningRegelmodell;
import no.nav.folketrygdloven.besteberegning.modell.output.AktivitetNøkkel;
import no.nav.folketrygdloven.besteberegning.modell.output.BeregnetMånedsgrunnlag;
import no.nav.folketrygdloven.besteberegning.modell.output.Inntekt;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FinnBesteMåneder.ID)
class FinnBesteMåneder extends LeafSpecification<BesteberegningRegelmodell> {

	// Trenger dokumentasjon på confluence og referanse til denne
	static final String ID = "14-7-3.1";
	static final String BESKRIVELSE = "Finn 6 måneder med høyest inntekt av de 10 siste";
	private static final int ANTALL_MÅNEDER_I_BESTEBERGNING = 6;
	public static final int MÅNEDER_I_ÅRET = 12;

	FinnBesteMåneder() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(BesteberegningRegelmodell regelmodell) {
		Map<String, Object> resultater = new HashMap<>();
		List<BeregnetMånedsgrunnlag> månedsgrunnlagListe = lagInntekterPrMåned(regelmodell, resultater);
		månedsgrunnlagListe.sort(BeregnetMånedsgrunnlag::compareTo);
		List<BeregnetMånedsgrunnlag> besteMåneder = månedsgrunnlagListe.subList(0, ANTALL_MÅNEDER_I_BESTEBERGNING);

		regelmodell.getOutput().setBesteMåneder(besteMåneder);
		resultater.put("AlleInntekter", månedsgrunnlagListe);
		resultater.put("BesteMåneder", besteMåneder);
		return beregnet(resultater);
	}

	private List<BeregnetMånedsgrunnlag> lagInntekterPrMåned(BesteberegningRegelmodell regelmodell, Map<String, Object> resultater) {
		var besteberegningInput = regelmodell.getInput();
		var inntektsgrunnlag = besteberegningInput.getInntektsgrunnlag();
		var inntekter = inntektsgrunnlag.getPeriodeinntekter();
		var perioderMedNæringsvirksomhet = regelmodell.getInput().getPerioderMedNæringsvirksomhet();
		var skjæringstidspunktOpptjening = besteberegningInput.getSkjæringstidspunktOpptjening();
		LocalDate sisteTilgjengeligeGSnittÅr = besteberegningInput.getGrunnbeløpSatser().stream()
				.map(Grunnbeløp::getFom)
				.max(Comparator.naturalOrder())
				.orElseThrow();
		LocalDate sisteGSnittÅrSomKanBrukes;
		if (sisteTilgjengeligeGSnittÅr.isAfter(skjæringstidspunktOpptjening)) {
			sisteGSnittÅrSomKanBrukes = skjæringstidspunktOpptjening;
		} else {
			sisteGSnittÅrSomKanBrukes = sisteTilgjengeligeGSnittÅr;
		}
		var gjenommsnittligPGI = FinnGjennomsnittligPGI.finnGjennomsnittligPGI(
				inntektsgrunnlag.sistePeriodeMedInntektFørDato(Inntektskilde.SIGRUN, skjæringstidspunktOpptjening).orElse(sisteGSnittÅrSomKanBrukes),
				besteberegningInput.getGrunnbeløpSatser(),
				inntektsgrunnlag, besteberegningInput.getGjeldendeGverdi(),
				resultater);
		var snittPGIPrMåned = gjenommsnittligPGI.divide(BigDecimal.valueOf(MÅNEDER_I_ÅRET), 10, RoundingMode.HALF_EVEN);

		List<BeregnetMånedsgrunnlag> månedsgrunnlagListe = new ArrayList<>();

		var førsteFom = skjæringstidspunktOpptjening.minusMonths(10).withDayOfMonth(1);
		var sisteFom = skjæringstidspunktOpptjening.minusMonths(1).withDayOfMonth(1);
		var fom = førsteFom;
		while (!fom.isAfter(sisteFom)) {
			var beregnetMånedsgrunnlag = lagGrunnlagForMåned(inntekter, perioderMedNæringsvirksomhet, snittPGIPrMåned, fom);
			månedsgrunnlagListe.add(beregnetMånedsgrunnlag);
			fom = fom.plusMonths(1);
		}
		return månedsgrunnlagListe;
	}

	private BeregnetMånedsgrunnlag lagGrunnlagForMåned(List<Periodeinntekt> inntekter,
	                                                   List<Periode> perioderMedNæringsvirksomhet,
	                                                   BigDecimal gjenommsnittligPGI, LocalDate fom) {
		var periode = Periode.of(fom, fom.with(TemporalAdjusters.lastDayOfMonth()));
		var beregnetMånedsgrunnlag = new BeregnetMånedsgrunnlag(YearMonth.from(fom));
		inntekter.stream()
				.filter(p -> p.getInntektskilde().equals(Inntektskilde.INNTEKTSKOMPONENTEN_SAMMENLIGNING))
				.filter(p -> AktivitetStatus.KUN_YTELSE.equals(p.getAktivitetStatus()) && p.getYtelse() != null)
				.filter(p -> p.erInnenforPeriode(periode))
				.map(this::mapTilInntekt).forEach(beregnetMånedsgrunnlag::leggTilInntekt);
		inntekter.stream()
				.filter(p -> p.getInntektskilde().equals(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING))
				.filter(p -> p.erInnenforPeriode(periode))
				.map(this::mapTilInntekt).forEach(beregnetMånedsgrunnlag::leggTilInntekt);
		inntekter.stream()
				.filter(p -> p.getInntektskilde().equals(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP))
				.filter(p -> periode.overlapper(p.getPeriode()))
				.map(periodeinntekt -> mapTilYtelseTilInntekt(periodeinntekt, periode)).forEach(beregnetMånedsgrunnlag::leggTilInntekt);
		finnInntektForNæring(perioderMedNæringsvirksomhet, gjenommsnittligPGI, periode, beregnetMånedsgrunnlag.finnSum())
				.ifPresent(beregnetMånedsgrunnlag::leggTilInntekt);
		return beregnetMånedsgrunnlag;
	}

	private Optional<Inntekt> finnInntektForNæring(List<Periode> perioderMedNæringsvirksomhet,
	                                               BigDecimal gjenommsnittligPGI,
	                                               Periode periode,
	                                               BigDecimal totalUtenNæring) {
		if (perioderMedNæringsvirksomhet.stream().anyMatch(p -> p.overlapper(periode))) {
			BigDecimal beregnetNæring = gjenommsnittligPGI.subtract(totalUtenNæring).max(BigDecimal.ZERO);
			return Optional.of(new Inntekt(AktivitetNøkkel.forType(Aktivitet.NÆRINGSINNTEKT), beregnetNæring));
		}
		return Optional.empty();
	}

	private Inntekt mapTilInntekt(Periodeinntekt periodeinntekt) {
		if (periodeinntekt.getArbeidsgiver().isPresent()) {
			return mapPeriodeinntektForArbeidstakerEllerFrilans(periodeinntekt);
		} else {
			var aktivitet = mapAktivitetstatusTilInntektType(periodeinntekt);
			var aktivitetNøkkel = AktivitetNøkkel.forType(aktivitet);
			var inntekt = periodeinntekt.getInntekt()
					.multiply(periodeinntekt.getInntektPeriodeType().getAntallPrÅr())
					.divide(BigDecimal.valueOf(MÅNEDER_I_ÅRET), 10, RoundingMode.HALF_EVEN);
			return new Inntekt(aktivitetNøkkel, inntekt);
		}
	}

	private Inntekt mapTilYtelseTilInntekt(Periodeinntekt periodeinntekt, Periode periode) {
		var aktivitet = mapAktivitetstatusTilInntektType(periodeinntekt);
		var aktivitetNøkkel = AktivitetNøkkel.forType(aktivitet);
		var inntekt = periodeinntekt.getInntekt();
		var overlappendePeriode = finnOverlappMellomPerioder(periodeinntekt.getPeriode(), periode)
				.orElseThrow(() -> new IllegalStateException("Forventer overlapp"));
		int antallVirkedager = Virkedager.beregnAntallVirkedager(overlappendePeriode);
		int totaltAntallVirkedagerForMeldekort = Virkedager.beregnAntallVirkedager(periodeinntekt.getPeriode());
		BigDecimal andelAvVirkedager = totaltAntallVirkedagerForMeldekort == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(antallVirkedager)
				.divide(BigDecimal.valueOf(totaltAntallVirkedagerForMeldekort), 10, RoundingMode.HALF_EVEN);
		return new Inntekt(aktivitetNøkkel, inntekt.multiply(andelAvVirkedager));
	}

	private Optional<Periode> finnOverlappMellomPerioder(Periode periode1, Periode periode2) {
		if (!periode1.overlapper(periode2)) {
			return Optional.empty();
		}
		LocalDate fom = periode1.getFom().isBefore(periode2.getFom()) ? periode2.getFom() : periode1.getFom();
		LocalDate tom = periode1.getTom().isBefore(periode2.getTom()) ? periode1.getTom() : periode2.getTom();
		return Optional.of(Periode.of(fom, tom));
	}

	private Aktivitet mapAktivitetstatusTilInntektType(Periodeinntekt periodeinntekt) {
		if (AktivitetStatus.FL.equals(periodeinntekt.getAktivitetStatus())) {
			return Aktivitet.FRILANSINNTEKT;
		} else if (AktivitetStatus.SN.equals(periodeinntekt.getAktivitetStatus())) {
			return Aktivitet.NÆRINGSINNTEKT;
		} else if (AktivitetStatus.KUN_YTELSE.equals(periodeinntekt.getAktivitetStatus())) {
			return mapYtelsetype(periodeinntekt.getYtelse());
		}
		return Aktivitet.DAGPENGEMOTTAKER;
	}

	private Aktivitet mapYtelsetype(RelatertYtelseType ytelse) {
		switch (ytelse) {
			case FORELDREPENGER:
				return Aktivitet.FORELDREPENGER_MOTTAKER;
			case SVANGERSKAPSPENGER:
				return Aktivitet.SVANGERSKAPSPENGER_MOTTAKER;
			case SYKEPENGER:
				return Aktivitet.SYKEPENGER_MOTTAKER;
			default:
				throw new IllegalStateException("Støtte på ukjent ytelse under besteberegning: " + ytelse.name());
		}
	}

	private Inntekt mapPeriodeinntektForArbeidstakerEllerFrilans(Periodeinntekt periodeinntekt) {
		var arbeidsforhold = periodeinntekt.getArbeidsgiver().orElseThrow(); // Sjekkes utenfor
		AktivitetNøkkel aktivitetNøkkel;
		if (arbeidsforhold.getOrgnr() != null) {
			aktivitetNøkkel = AktivitetNøkkel.forOrganisasjon(arbeidsforhold.getOrgnr(), arbeidsforhold.getArbeidsforholdId());
		} else if (arbeidsforhold.getAktørId() != null) {
			aktivitetNøkkel = AktivitetNøkkel.forPrivatperson(arbeidsforhold.getAktørId(), arbeidsforhold.getArbeidsforholdId());
		} else {
			aktivitetNøkkel = AktivitetNøkkel.forType(arbeidsforhold.getAktivitet());
		}
		var inntektPeriodeType = periodeinntekt.getInntektPeriodeType();
		var antallPrÅr = inntektPeriodeType.getAntallPrÅr();
		var inntekt = periodeinntekt.getInntekt().multiply(antallPrÅr)
				.divide(BigDecimal.valueOf(MÅNEDER_I_ÅRET), 10, RoundingMode.HALF_EVEN);
		return new Inntekt(aktivitetNøkkel, inntekt);
	}

}
