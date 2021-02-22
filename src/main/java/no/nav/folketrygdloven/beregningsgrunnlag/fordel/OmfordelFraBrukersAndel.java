package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrArbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

/**
 * Omfordeler fra en generell brukers andel til en aktivitet/andel som er det er søkt for.
 * <p>
 * En brukers andel blir brukt i situasjoner der bruker er midlertidig inaktiv eller kommer direkte fra ytelse uten arbeidsforhold.
 * <p>
 * Dersom det søkes utbetaling for andre aktiviteter i løpet av ytelseperioden skal beregningsgrunnlag omfordeles til disse.
 * <p>
 * Omfordeling gjøres slik at hele grunnlaget fra brukers andel settes på andelen med lavest avkortingprioritet (den som avkortes sist).
 */
class OmfordelFraBrukersAndel extends LeafSpecification<BeregningsgrunnlagPeriode> {

	public static final String ID = "OMFORDEL_FRA_BA";
	public static final String BESKRIVELSE = "Flytt beregningsgrunnlag fra brukers andel til aktivitetstatus med høyere prioritet";
	private final Comparator<BeregningsgrunnlagPrStatus> AVKORTING_COMPARATOR = Comparator.comparingInt(a -> a.getAktivitetStatus().getAvkortingPrioritet());

	OmfordelFraBrukersAndel() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
		Map<String, Object> resultater = new HashMap<>();
		var brukersAndel = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatus(AktivitetStatus.BA);
		var statusSomAvkortesSistUtenArbeid = finnStatusÅFlytteTil(beregningsgrunnlagPeriode);
		resultater.put("aktivitetstatusSomFlyttesTilFraInaktiv", statusSomAvkortesSistUtenArbeid.getAktivitetStatus());
		resultater.put("aktivitetstatusSomFlyttesFraInaktiv", brukersAndel.getBruttoPrÅr());
		if (AktivitetStatus.ATFL.equals(statusSomAvkortesSistUtenArbeid.getAktivitetStatus())) {
			var frilans = statusSomAvkortesSistUtenArbeid.getFrilansArbeidsforhold()
					.orElseThrow(() -> new IllegalStateException("Forventer at man har frilans"));
			flyttTilFrilans(brukersAndel, frilans);
		} else {
			flyttTilAnnenStatus(brukersAndel, statusSomAvkortesSistUtenArbeid);
		}
		settBrukersAndelTil0(brukersAndel);
		return beregnet(resultater);
	}

	private void settBrukersAndelTil0(BeregningsgrunnlagPrStatus brukersAndel) {
		BeregningsgrunnlagPrStatus.builder(brukersAndel)
				.medFordeltPrÅr(BigDecimal.ZERO)
				.build();
	}

	private void flyttTilAnnenStatus(BeregningsgrunnlagPrStatus brukersAndel, BeregningsgrunnlagPrStatus statusSomAvkortesSistUtenArbeid) {
		BeregningsgrunnlagPrStatus.builder(statusSomAvkortesSistUtenArbeid)
				.medInntektskategori(brukersAndel.getInntektskategori())
				.medFordeltPrÅr(statusSomAvkortesSistUtenArbeid.getBruttoPrÅr().add(brukersAndel.getBruttoPrÅr()))
				.build();
	}

	private void flyttTilFrilans(BeregningsgrunnlagPrStatus brukersAndel, BeregningsgrunnlagPrArbeidsforhold frilansArbeidsforhold) {
		BeregningsgrunnlagPrArbeidsforhold.builder(frilansArbeidsforhold)
				.medInntektskategori(brukersAndel.getInntektskategori())
				.medFordeltPrÅr(frilansArbeidsforhold.getBruttoPrÅr().orElse(BigDecimal.ZERO).add(brukersAndel.getBruttoPrÅr()))
				.build();
	}

	private BeregningsgrunnlagPrStatus finnStatusÅFlytteTil(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
		var statusSomAvkortesSistUtenArbeid = beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusSomSkalBrukes()
				.stream()
				.filter(a -> !AktivitetStatus.ATFL.equals(a.getAktivitetStatus()) || a.getFrilansArbeidsforhold().isPresent())
				.min(AVKORTING_COMPARATOR)
				.orElseThrow(() -> new IllegalStateException("Forventet å ha en aktivitet å flytte til som ikke er arbeid"));
		return statusSomAvkortesSistUtenArbeid;
	}

}
