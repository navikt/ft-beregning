package no.nav.folketrygdloven.beregningsgrunnlag.inaktiv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

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
		List<Periodeinntekt> inntektsmeldinger = finnInntektsmeldinger(grunnlag);
		var innrapporterteInntekter = finnOverlappendePeriodeInntekterFraInntektskomponenten(grunnlag);

		if (inntektsmeldinger.isEmpty() && innrapporterteInntekter.isEmpty()) {
			return Optional.empty();
		}

		BigDecimal årsinntektFraInntektsmelding = finnÅrsinntektFraInntektsmeldinger(inntektsmeldinger);
		BigDecimal årsinntektFraAOrdningen = finnÅrsinntektFraAOrdningen(grunnlag, innrapporterteInntekter);

		return Optional.of(Periodeinntekt.builder()
				.medPeriode(Periode.of(grunnlag.getSkjæringstidspunkt(), grunnlag.getSkjæringstidspunkt()))
				.medInntekt(årsinntektFraInntektsmelding.add(årsinntektFraAOrdningen))
				.medInntektskildeOgPeriodeType(Inntektskilde.SØKNAD)
				.build());
	}

	private List<Periodeinntekt> finnOverlappendePeriodeInntekterFraInntektskomponenten(BeregningsgrunnlagPeriode grunnlag) {
		return grunnlag.getInntektsgrunnlag().getPeriodeinntekter().stream()
				.filter(p -> p.getInntektskilde().equals(Inntektskilde.INNTEKTSKOMPONENTEN_BEREGNING))
				.filter(p -> p.getArbeidsgiver().isPresent() && p.getArbeidsgiver().get().getAnsettelsesPeriode().inneholder(grunnlag.getSkjæringstidspunkt().minusDays(1)))
				.filter(p -> p.inneholder(grunnlag.getSkjæringstidspunkt()))
				.toList();
	}

	private List<Periodeinntekt> finnInntektsmeldinger(BeregningsgrunnlagPeriode grunnlag) {
		var inntektsmeldinger = grunnlag.getInntektsgrunnlag().getPeriodeinntekter()
				.stream().filter(p -> p.getInntektskilde().equals(Inntektskilde.INNTEKTSMELDING))
				.filter(im -> im.getArbeidsgiver().isPresent() && im.getArbeidsgiver().get().getAnsettelsesPeriode().inneholder(grunnlag.getSkjæringstidspunkt().minusDays(1)))
				.toList();
		return inntektsmeldinger;
	}

	private BigDecimal finnÅrsinntektFraInntektsmeldinger(List<Periodeinntekt> inntektsmeldinger) {
		var årsinntektFraInntektsmelding = inntektsmeldinger.stream()
				.map(im -> im.getInntekt().multiply(im.getInntektPeriodeType().getAntallPrÅr()))
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
		return årsinntektFraInntektsmelding;
	}

	private BigDecimal finnÅrsinntektFraAOrdningen(BeregningsgrunnlagPeriode grunnlag, List<Periodeinntekt> innrapporterteInntekter) {
		return innrapporterteInntekter.stream().map(i -> {
					var inntekt = i.getInntekt();
					var arbeidsgiver = i.getArbeidsgiver();
					if (arbeidsgiver.isPresent()) {
						int virkedagerIPeriode = finnVirkedagerMedArbeidForInntektsperiode(i.getPeriode(), arbeidsgiver.get());
						if (virkedagerIPeriode > 0) {
							return inntekt.divide(BigDecimal.valueOf(virkedagerIPeriode), 10, RoundingMode.HALF_UP).multiply(grunnlag.getBeregningsgrunnlag().getYtelsedagerPrÅr());
						}
					}
					return BigDecimal.ZERO;
				}).reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
	}

	private int finnVirkedagerMedArbeidForInntektsperiode(Periode inntektsperiode, Arbeidsforhold arbeidsgiver) {
		var ansettelsesPeriode = arbeidsgiver.getAnsettelsesPeriode();
		var ansettelsesperiodeFom = ansettelsesPeriode.getFom();
		var ansattPeriodeFørStp = Periode.of(ansettelsesperiodeFom, inntektsperiode.getTom());
		return Virkedager.beregnAntallVirkedager(ansattPeriodeFørStp);
	}
}
