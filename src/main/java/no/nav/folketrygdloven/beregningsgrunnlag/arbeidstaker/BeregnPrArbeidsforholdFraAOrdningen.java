package no.nav.folketrygdloven.beregningsgrunnlag.arbeidstaker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ytelse.YtelsesSpesifiktGrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(BeregnPrArbeidsforholdFraAOrdningen.ID)
class BeregnPrArbeidsforholdFraAOrdningen extends LeafSpecification<BeregningsgrunnlagPeriode> {

	static final String ID = "FB_BR 14.3";
	static final String BESKRIVELSE = "Rapportert inntekt = snitt av mnd-inntekter i beregningsperioden * 12";
	private BeregningsgrunnlagPrArbeidsforhold arbeidsforhold;

	BeregnPrArbeidsforholdFraAOrdningen(BeregningsgrunnlagPrArbeidsforhold arbeidsforhold) {
		super(ID, BESKRIVELSE);
		Objects.requireNonNull(arbeidsforhold, "arbeidsforhold");
		this.arbeidsforhold = arbeidsforhold;
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
		Periode bp = arbeidsforhold.getBeregningsperiode();
		if (bp == null) {
			throw new IllegalStateException("Beregningsperiode mangler, kan ikke fastsette beregningsgrunnlag for arbeidsforhold");
		}
		var snittFraBeregningsperiodenPrÅr = finnSnittinntektFraBeregningsperiodenPrÅr(bp, grunnlag.getInntektsgrunnlag());

		BigDecimal andelFraAOrdningenPrÅr = finnAndelAvBeregnet(snittFraBeregningsperiodenPrÅr, arbeidsforhold, grunnlag);
		BeregningsgrunnlagPrArbeidsforhold.builder(arbeidsforhold)
				.medBeregnetPrÅr(andelFraAOrdningenPrÅr)
				.build();
		Map<String, Object> resultater = new HashMap<>();
		resultater.put("beregnetPrÅr", arbeidsforhold.getBeregnetPrÅr());
		resultater.put("arbeidsforhold", arbeidsforhold.getBeskrivelse());
		return beregnet(resultater);
	}

	/**
	 * Beregner gjennomsnittlig inntekt i beregningsperioden
	 * <p>
	 * Ved beregningsperiode som strekker seg over flere måneder brukes alle hele måneder i beregning av snittinntekt.
	 * <p>
	 * Ved beregningsperiode som er kortere enn en måned beregnes snittinnekt i måned ved å bruke antall gjenværende virkedager i måned og totalt antall
	 * virkedager i måned.
	 *
	 * @param bp               Beregningsperiode
	 * @param inntektsgrunnlag Inntektsgrunnlag
	 * @return Snittinntekt fra beregningsperioden
	 */
	private BigDecimal finnSnittinntektFraBeregningsperiodenPrÅr(Periode bp, Inntektsgrunnlag inntektsgrunnlag) {
		int varighetMåneder = finnHeleMåneder(bp);
		if (varighetMåneder > 0) {
			List<Periodeinntekt> inntekter = inntektsgrunnlag.getPeriodeinntekter(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, bp.getTom(), varighetMåneder);
			BigDecimal sum = inntekter.stream()
					.map(Periodeinntekt::getInntekt)
					.reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
			return sum.multiply(BigDecimal.valueOf(12)).divide(BigDecimal.valueOf(varighetMåneder), 10, RoundingMode.HALF_EVEN);
		} else {
			List<Periodeinntekt> inntekter = inntektsgrunnlag.getPeriodeinntekter(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING, arbeidsforhold, bp.getTom(), 1);
			if (inntekter.size() > 1) {
				throw new IllegalStateException("Forventet kun en månedsinntekt");
			}
			if (inntekter.isEmpty()) {
				return BigDecimal.ZERO;
			}
			return beregnÅrsinntektFraGjenværendeDager(bp, inntekter.get(0));
		}
	}

	private BigDecimal beregnÅrsinntektFraGjenværendeDager(Periode bp, Periodeinntekt inntekt) {
		int gjenværendeVirkedager = finnGjenværendeVirkedagerIFørsteMåned(bp);
		if (gjenværendeVirkedager > 0) {
			return finnÅrsinntektFraDelvisMånedsinntektMedAntallDager(bp, inntekt, gjenværendeVirkedager);
		}
		int gjenværendeDager = bp.getFom().with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth() - bp.getFom().getDayOfMonth() + 1;
		return finnÅrsinntektFraDelvisMånedsinntektMedAntallDager(bp, inntekt, gjenværendeDager);
	}

	private BigDecimal finnÅrsinntektFraDelvisMånedsinntektMedAntallDager(Periode bp, Periodeinntekt inntekt, int gjenværendeVirkedager) {
		BigDecimal dagsats = inntekt.getInntekt().divide(BigDecimal.valueOf(gjenværendeVirkedager), 10, RoundingMode.HALF_EVEN);
		int virkedagerIHeleMåned = Virkedager.beregnAntallVirkedager(bp.getFom().with(TemporalAdjusters.firstDayOfMonth()), bp.getFom().with(TemporalAdjusters.lastDayOfMonth()));
		return dagsats.multiply(BigDecimal.valueOf(virkedagerIHeleMåned)).multiply(BigDecimal.valueOf(12));
	}

	private int finnGjenværendeVirkedagerIFørsteMåned(Periode bp) {
		return Virkedager.beregnAntallVirkedager(bp.getFom(), bp.getFom().with(TemporalAdjusters.lastDayOfMonth()));
	}

	private int finnHeleMåneder(Periode bp) {
		int antallMåneder = 0;
		LocalDate date = bp.getFom().minusDays(1).with(TemporalAdjusters.lastDayOfMonth());
		while (date.isBefore(bp.getTom())) {
			antallMåneder++;
			date = date.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
		}
		return antallMåneder;
	}

	/**
	 * I tilfelle der det er omsorgspenger/pleiepenger og det er kun mottatt inntektsmelding for enkelt arbeidsforhold ho en arbeidsgiver
	 * skal resterende arbeidsforhold fordele restbeløpet fra a-ordningen mellom seg (https://jira.adeo.no/browse/TSF-1153).
	 * <p>
	 * I alle andre caser gis det fulle snittbeløpet til dette arbeidsforholdet.
	 *
	 * @param beregnetPrÅr   Snitt de siste 3 månedene fra a-ordningen
	 * @param arbeidsforhold Arbeidsforhold
	 * @param periode        Periode
	 * @return Andel av snitt fra a-ordningen
	 */
	private BigDecimal finnAndelAvBeregnet(BigDecimal beregnetPrÅr, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPeriode periode) {
		var ytelsesSpesifiktGrunnlag = periode.getBeregningsgrunnlag().getYtelsesSpesifiktGrunnlag();
		if (!arbeidsforhold.erFrilanser() && arbeidsforhold.getArbeidsgiverId() != null && ytelsesSpesifiktGrunnlag.erKap9Ytelse()) {
			return fordelRestinntektFraAOrdningen(beregnetPrÅr, arbeidsforhold, periode);
		}
		return beregnetPrÅr;
	}

	private BigDecimal fordelRestinntektFraAOrdningen(BigDecimal beregnetPrÅr, BeregningsgrunnlagPrArbeidsforhold arbeidsforhold, BeregningsgrunnlagPeriode periode) {
		Inntektsgrunnlag inntektsgrunnlag = periode.getInntektsgrunnlag();
		List<BeregningsgrunnlagPrArbeidsforhold> arbeidsforholdISammeOrg = periode.getBeregningsgrunnlagPrStatus(AktivitetStatus.ATFL)
				.getArbeidsforholdIkkeFrilans()
				.stream()
				.filter(a -> a.getArbeidsgiverId() != null)
				.filter(a -> a.getArbeidsgiverId().equals(arbeidsforhold.getArbeidsgiverId()))
				.collect(Collectors.toList());
		BigDecimal beløpFraInntektsmeldingPrÅr = arbeidsforholdISammeOrg.stream()
				.map(inntektsgrunnlag::getInntektFraInntektsmelding)
				.map(beløp -> beløp.multiply(BigDecimal.valueOf(12)))
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);

		if (beløpFraInntektsmeldingPrÅr.compareTo(beregnetPrÅr) > 0) {
			return BigDecimal.ZERO;
		}

		BigDecimal restFraAOrdningen = beregnetPrÅr.subtract(beløpFraInntektsmeldingPrÅr);

		long antallArbeidsforholdUtenInntektsmelding = arbeidsforholdISammeOrg.stream()
				.filter(a -> inntektsgrunnlag.getInntektFraInntektsmelding(a) == null || inntektsgrunnlag.getInntektFraInntektsmelding(a).compareTo(BigDecimal.ZERO) == 0)
				.count();

		if (antallArbeidsforholdUtenInntektsmelding == 0) {
			throw new IllegalStateException("Kan ikke beregne andel fra aordningen når alle andeler for arbeidsgiver med orgnr " + arbeidsforhold.getArbeidsgiverId() +
					" har inntektsmelding");
		}

		return restFraAOrdningen.divide(BigDecimal.valueOf(antallArbeidsforholdUtenInntektsmelding), RoundingMode.HALF_EVEN);
	}
}
