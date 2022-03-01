package no.nav.folketrygdloven.beregningsgrunnlag.ytelse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskilde;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Periodeinntekt;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

/**
 * Henter ytelse med kilde YTELSE_VEDTAK og summerer for inntektskategori til andelen.
 */
@RuleDocumentation(BeregnFraYtelsevedtak.ID)
class BeregnFraYtelsevedtak extends LeafSpecification<BeregningsgrunnlagPeriode> {

	static final String ID = "FB_BR 30.1.2";
	static final String BESKRIVELSE = "Fastsetter beregningsgrunnlag fra ytelsevedtak";
	private final BeregningsgrunnlagPrStatus statusandel;

	BeregnFraYtelsevedtak(BeregningsgrunnlagPrStatus statusandel) {
		super(ID, BESKRIVELSE);
		Objects.requireNonNull(statusandel, "statusandel");
		this.statusandel = statusandel;

	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {

		var inntektsgrunnlag = grunnlag.getInntektsgrunnlag();
		var skjæringstidspunkt = grunnlag.getBeregningsgrunnlag().getSkjæringstidspunkt();
		var inntektFraYtelseVedtak = inntektsgrunnlag.getPeriodeinntekter(Inntektskilde.YTELSE_VEDTAK, skjæringstidspunkt.minusDays(1));

		// Antar at inntektene her allerede er skalert mot gradering i ytelse
		var beregnet = inntektFraYtelseVedtak.stream()
				.filter(i -> i.getAktivitetStatus().equals(statusandel.getInntektskategori().getAktivitetStatus()))
				.map(Periodeinntekt::getInntekt)
				.map(i -> i.multiply(Inntektskilde.YTELSE_VEDTAK.getInntektPeriodeType().getAntallPrÅr()))
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);

		BeregningsgrunnlagPrStatus.builder(statusandel).medBeregnetPrÅr(beregnet);

		Map<String, Object> resultater = new HashMap<>();
		resultater.put("inntektskategori", statusandel.getInntektskategori());
		resultater.put("beregnetPrÅr", statusandel.getBeregnetPrÅr());
		return beregnet(resultater);
	}
}
