package no.nav.folketrygdloven.besteberegning;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.InntektPeriodeType;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.FinnGjennomsnittligPGI;
import no.nav.folketrygdloven.besteberegning.modell.BesteberegningRegelmodell;
import no.nav.folketrygdloven.besteberegning.modell.input.BesteberegningInput;
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
		BesteberegningInput besteberegningInput = regelmodell.getInput();
		Inntektsgrunnlag inntektsgrunnlag = besteberegningInput.getInntektsgrunnlag();
		List<Periodeinntekt> inntekter = inntektsgrunnlag.getPeriodeinntekter();
		List<Periode> perioderMedNæringsvirksomhet = regelmodell.getInput().getPerioderMedNæringsvirksomhet();
		LocalDate skjæringstidspunktOpptjening = besteberegningInput.getSkjæringstidspunktOpptjening();

		BigDecimal gjenommsnittligPGI = FinnGjennomsnittligPGI.finnGjennomsnittligPGI(
				inntektsgrunnlag.sistePeriodeMedInntektFørDato(Inntektskilde.SIGRUN, skjæringstidspunktOpptjening).orElse(skjæringstidspunktOpptjening),
				besteberegningInput.getGrunnbeløpSatser(),
				inntektsgrunnlag, besteberegningInput.getGjeldendeGverdi(),
				resultater);

		List<BeregnetMånedsgrunnlag> månedsgrunnlagListe = new ArrayList<>();

		LocalDate førsteFom = skjæringstidspunktOpptjening.minusMonths(10).withDayOfMonth(1);
		LocalDate sisteFom = skjæringstidspunktOpptjening.minusMonths(1).withDayOfMonth(1);
		LocalDate fom = førsteFom;
		while (!fom.isAfter(sisteFom)) {
			BeregnetMånedsgrunnlag beregnetMånedsgrunnlag = lagGrunnlagForMåned(inntekter, perioderMedNæringsvirksomhet, gjenommsnittligPGI, fom);
			månedsgrunnlagListe.add(beregnetMånedsgrunnlag);
			fom = fom.plusMonths(1);
		}
		return månedsgrunnlagListe;
	}

	private BeregnetMånedsgrunnlag lagGrunnlagForMåned(List<Periodeinntekt> inntekter, List<Periode> perioderMedNæringsvirksomhet, BigDecimal gjenommsnittligPGI, LocalDate fom) {
		Periode periode = Periode.of(fom, fom.with(TemporalAdjusters.lastDayOfMonth()));
		BeregnetMånedsgrunnlag beregnetMånedsgrunnlag = new BeregnetMånedsgrunnlag(YearMonth.from(fom));
		inntekter.stream()
				.filter(p -> p.getInntektskilde().equals(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING))
				.filter(p -> p.erInnenforPeriode(periode))
				.map(this::mapTilInntekt).forEach(beregnetMånedsgrunnlag::leggTilInntekt);
		inntekter.stream()
				.filter(p -> p.getInntektskilde().equals(Inntektskilde.TILSTØTENDE_YTELSE_DP_AAP))
				.filter(p -> p.erInnenforPeriode(periode))
				.map(this::mapTilInntekt).forEach(beregnetMånedsgrunnlag::leggTilInntekt);
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
			var aktivitet = mapAktivitetstatusTilInntektType(periodeinntekt.getAktivitetStatus());
			var aktivitetNøkkel = AktivitetNøkkel.forType(aktivitet);
			var inntektPeriodeType = periodeinntekt.getInntektPeriodeType();
			BigDecimal inntekt = periodeinntekt.getInntekt()
					.multiply(inntektPeriodeType.getAntallPrÅr())
					.multiply(periodeinntekt.getUtbetalingsgrad().orElse(BigDecimal.valueOf(200))
							.divide(BigDecimal.valueOf(200), 10, RoundingMode.HALF_EVEN));
			return new Inntekt(aktivitetNøkkel, inntekt);
		}
	}

	private Aktivitet mapAktivitetstatusTilInntektType(AktivitetStatus aktivitetStatus) {
		if (AktivitetStatus.FL.equals(aktivitetStatus)) {
			return Aktivitet.FRILANSINNTEKT;
		}
		if (AktivitetStatus.SN.equals(aktivitetStatus)) {
			return Aktivitet.NÆRINGSINNTEKT;
		}
		return Aktivitet.DAGPENGEMOTTAKER;
	}

	private Inntekt mapPeriodeinntektForArbeidstakerEllerFrilans(Periodeinntekt periodeinntekt) {
		Arbeidsforhold arbeidsforhold = periodeinntekt.getArbeidsgiver().get();
		AktivitetNøkkel aktivitetNøkkel;
		if (arbeidsforhold.getOrgnr() != null) {
			aktivitetNøkkel = AktivitetNøkkel.forOrganisasjon(arbeidsforhold.getOrgnr(), arbeidsforhold.getArbeidsforholdId());
		} else if (arbeidsforhold.getAktørId() != null) {
			aktivitetNøkkel = AktivitetNøkkel.forPrivatperson(arbeidsforhold.getAktørId(), arbeidsforhold.getArbeidsforholdId());
		} else {
			aktivitetNøkkel = AktivitetNøkkel.forType(arbeidsforhold.getAktivitet());
		}
		InntektPeriodeType inntektPeriodeType = periodeinntekt.getInntektPeriodeType();
		BigDecimal antallPrÅr = inntektPeriodeType.getAntallPrÅr();
		return new Inntekt(aktivitetNøkkel, periodeinntekt.getInntekt().multiply(antallPrÅr));
	}

}
