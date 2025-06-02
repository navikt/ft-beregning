package no.nav.folketrygdloven.beregningsgrunnlag.inaktiv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Periode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.selvstendig.FinnRapporterteInntekter;
import no.nav.folketrygdloven.beregningsgrunnlag.util.Virkedager;


public class FinnRapporterteInntekterForInaktiv implements FinnRapporterteInntekter {

	@Override
	public Optional<Periodeinntekt> finnRapportertInntekt(BeregningsgrunnlagPeriode grunnlag) {
        var inntektsmeldinger = finnInntektsmeldinger(grunnlag);
		var innrapporterteInntekter = finnOverlappendePeriodeInntekterFraInntektskomponenten(grunnlag);

		if (inntektsmeldinger.isEmpty() && innrapporterteInntekter.isEmpty()) {
			return Optional.empty();
		}

        var årsinntektFraInntektsmelding = finnÅrsinntektFraInntektsmeldinger(inntektsmeldinger);
		var arbeidsgivereMedInntektsmelding = inntektsmeldinger.stream()
				.map(Periodeinntekt::getArbeidsgiver)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
        var årsinntektFraAOrdningen = finnÅrsinntektFraAOrdningen(grunnlag, innrapporterteInntekter, arbeidsgivereMedInntektsmelding);

		return Optional.of(Periodeinntekt.builder()
				.medPeriode(Periode.of(grunnlag.getSkjæringstidspunkt(), grunnlag.getSkjæringstidspunkt()))
				.medInntekt(årsinntektFraInntektsmelding.add(årsinntektFraAOrdningen))
				.medInntektskildeOgPeriodeType(Inntektskilde.SØKNAD)
				.build());
	}

	private List<Periodeinntekt> finnOverlappendePeriodeInntekterFraInntektskomponenten(BeregningsgrunnlagPeriode grunnlag) {
		return grunnlag.getInntektsgrunnlag().getPeriodeinntekter().stream()
				.filter(p -> p.getInntektskilde().equals(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING))
				.filter(p -> p.getArbeidsgiver().isPresent() && p.getArbeidsgiver().get().getAnsettelsesPeriode().map(periode -> periode.inneholder(grunnlag.getSkjæringstidspunkt().minusDays(1))).orElse(false))
				.filter(p -> starterFørStp(grunnlag, p) && erIkkeEldreEnn4MånederFørStp(grunnlag, p) && harMinstEnVirkedag(p))
				.toList();
	}

	private boolean harMinstEnVirkedag(Periodeinntekt p) {
		var ansettelsesperiode = p.getArbeidsgiver().flatMap(Arbeidsforhold::getAnsettelsesPeriode).filter(a -> !a.getFom().isAfter(p.getTom()));
		return ansettelsesperiode.filter(aa -> Virkedager.beregnAntallVirkedager(aa.getFom(), p.getTom()) > 0).isPresent();
	}

	private boolean starterFørStp(BeregningsgrunnlagPeriode grunnlag, Periodeinntekt p) {
		return p.getPeriode().getFom().isBefore(grunnlag.getSkjæringstidspunkt());
	}

	private boolean erIkkeEldreEnn4MånederFørStp(BeregningsgrunnlagPeriode grunnlag, Periodeinntekt p) {
		return p.getPeriode().getFom().plusMonths(4).isAfter(grunnlag.getSkjæringstidspunkt());
	}

	private List<Periodeinntekt> finnInntektsmeldinger(BeregningsgrunnlagPeriode grunnlag) {
		return grunnlag.getInntektsgrunnlag().getPeriodeinntekter()
				.stream().filter(p -> p.getInntektskilde().equals(Inntektskilde.INNTEKTSMELDING))
				.filter(im -> im.getArbeidsgiver().isPresent() && im.getArbeidsgiver().get().getAnsettelsesPeriode().map(periode -> periode.inneholder(grunnlag.getSkjæringstidspunkt().minusDays(1))).orElse(false))
				.toList();
	}

	private BigDecimal finnÅrsinntektFraInntektsmeldinger(List<Periodeinntekt> inntektsmeldinger) {
		return inntektsmeldinger.stream()
				.map(im -> im.getInntekt().multiply(im.getInntektPeriodeType().getAntallPrÅr()))
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
	}

	private BigDecimal finnÅrsinntektFraAOrdningen(BeregningsgrunnlagPeriode grunnlag,
	                                               List<Periodeinntekt> innrapporterteInntekter,
	                                               Set<Arbeidsforhold> arbeidsgivereMedInntektsmelding) {
		var gruppertPrArbeidsgiver = grupperPåArbeidsgiver(innrapporterteInntekter, arbeidsgivereMedInntektsmelding);
		return gruppertPrArbeidsgiver.entrySet().stream()
				.map(e -> mapTilÅrsinntekt(grunnlag, e.getKey(), e.getValue()))
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
	}

	private BigDecimal mapTilÅrsinntekt(BeregningsgrunnlagPeriode grunnlag, Arbeidsforhold arbeidsgiver, List<Periodeinntekt> inntekter) {
		if (harFlereMånederMedInntekt(inntekter) && harInntektISammeMånedSomStp(grunnlag, inntekter)) {
			// Finner nest nyeste for å finne en inntekt der arbeidstaker var ansatt til slutten av måneden
			return beregnForInntekt(grunnlag, arbeidsgiver, finnNestNyesteMånedMedInntekt(inntekter));
		} else {
			return beregnForInntekt(grunnlag, arbeidsgiver, finnNyesteMånedMedInntekt(inntekter));
		}
	}

	private Map<Arbeidsforhold, List<Periodeinntekt>> grupperPåArbeidsgiver(List<Periodeinntekt> innrapporterteInntekter, Set<Arbeidsforhold> arbeidsgivereMedInntektsmelding) {
		return innrapporterteInntekter.stream()
				.filter(i -> i.getArbeidsgiver().isPresent())
				.filter(i -> arbeidsgivereMedInntektsmelding.stream().noneMatch(a -> a.getArbeidsgiverId().equals(i.getArbeidsgiver().get().getArbeidsgiverId())))
				.collect(Collectors.groupingBy(i -> i.getArbeidsgiver().get()));
	}

	private Periodeinntekt finnNestNyesteMånedMedInntekt(List<Periodeinntekt> inntekter) {
		var sorterte = inntekter.stream().sorted(Comparator.comparing(Periodeinntekt::getPeriode).reversed())
				.toList();
		return sorterte.get(1);
	}

	private Periodeinntekt finnNyesteMånedMedInntekt(List<Periodeinntekt> inntekter) {
		var sorterte = inntekter.stream().sorted(Comparator.comparing(Periodeinntekt::getPeriode).reversed())
				.toList();
		return sorterte.get(0);
	}

	private boolean harFlereMånederMedInntekt(List<Periodeinntekt> inntekter) {
		return inntekter.size() > 1;
	}

	private boolean harInntektISammeMånedSomStp(BeregningsgrunnlagPeriode grunnlag, List<Periodeinntekt> inntekter) {
		return inntekter.stream().anyMatch(i -> i.getPeriode().inneholder(grunnlag.getSkjæringstidspunkt()));
	}

	private BigDecimal beregnForInntekt(BeregningsgrunnlagPeriode grunnlag, Arbeidsforhold arbeidsgiver, Periodeinntekt nestNyeste) {
        var virkedagerIPeriode = finnVirkedagerMedArbeidForInntektsperiode(nestNyeste.getPeriode(), arbeidsgiver);
		if (virkedagerIPeriode > 0) {
			var inntekt = nestNyeste.getInntekt();
			return inntekt.divide(BigDecimal.valueOf(virkedagerIPeriode), 10, RoundingMode.HALF_UP).multiply(grunnlag.getBeregningsgrunnlag().getYtelsedagerPrÅr());
		}
		return BigDecimal.ZERO;
	}

	private int finnVirkedagerMedArbeidForInntektsperiode(Periode inntektsperiode, Arbeidsforhold arbeidsgiver) {
		var ansettelsesPeriode = arbeidsgiver.getAnsettelsesPeriode().orElseThrow(() -> new IllegalStateException("Forventer inntektsperiode med ansettelsesperiode"));
		var ansettelsesperiodeFom = ansettelsesPeriode.getFom();
		var fom = ansettelsesperiodeFom.isBefore(inntektsperiode.getFom()) ? inntektsperiode.getFom() : ansettelsesperiodeFom;
		var periodeForInntekt = Periode.of(fom, inntektsperiode.getTom());
		return Virkedager.beregnAntallVirkedager(periodeForInntekt);
	}
}
