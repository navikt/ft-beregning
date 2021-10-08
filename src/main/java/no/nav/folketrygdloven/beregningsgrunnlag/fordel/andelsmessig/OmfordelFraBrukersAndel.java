package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatus;
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
class OmfordelFraBrukersAndel extends LeafSpecification<FordelPeriodeModell> {

	public static final String ID = "OMFORDEL_FRA_BA";
	public static final String BESKRIVELSE = "Flytt beregningsgrunnlag fra brukers andel til aktivitetstatus med høyere prioritet";
	private final Comparator<FordelAndelModell> AVKORTING_COMPARATOR = Comparator.comparingInt(a -> a.getAktivitetStatus().getAvkortingPrioritet());

	OmfordelFraBrukersAndel() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(FordelPeriodeModell beregningsgrunnlagPeriode) {
		Map<String, Object> resultater = new HashMap<>();
		var brukersAndel = beregningsgrunnlagPeriode.getEnesteAndelForStatus(AktivitetStatus.BA)
				.orElseThrow(() -> new IllegalStateException("Forventer å finne en brukers andel"));
		var statusSomAvkortesSistUtenArbeid = finnStatusÅFlytteTil(beregningsgrunnlagPeriode);
		resultater.put("aktivitetstatusSomFlyttesTilFraInaktiv", statusSomAvkortesSistUtenArbeid.getAktivitetStatus());
		resultater.put("aktivitetstatusSomFlyttesFraInaktiv", brukersAndel.getBruttoPrÅr());
		if (AktivitetStatus.FL.equals(statusSomAvkortesSistUtenArbeid.getAktivitetStatus())) {
			flyttTilFrilans(brukersAndel, statusSomAvkortesSistUtenArbeid);
		} else {
			flyttTilAnnenStatus(brukersAndel, statusSomAvkortesSistUtenArbeid);
		}
		settBrukersAndelTil0(brukersAndel);
		return beregnet(resultater);
	}

	private void settBrukersAndelTil0(FordelAndelModell brukersAndel) {
		FordelAndelModell.oppdater(brukersAndel)
				.medFordeltPrÅr(BigDecimal.ZERO)
				.build();
	}

	private void flyttTilAnnenStatus(FordelAndelModell brukersAndel, FordelAndelModell statusSomAvkortesSistUtenArbeid) {
		FordelAndelModell.oppdater(statusSomAvkortesSistUtenArbeid)
				.medInntektskategori(brukersAndel.getInntektskategori())
				.medFordeltPrÅr(statusSomAvkortesSistUtenArbeid.getBruttoPrÅr().orElse(BigDecimal.ZERO).add(brukersAndel.getBruttoPrÅr().orElse(BigDecimal.ZERO)))
				.build();
	}

	private void flyttTilFrilans(FordelAndelModell brukersAndel, FordelAndelModell frilansArbeidsforhold) {
		FordelAndelModell.oppdater(frilansArbeidsforhold)
				.medInntektskategori(brukersAndel.getInntektskategori())
				.medFordeltPrÅr(frilansArbeidsforhold.getBruttoPrÅr().orElse(BigDecimal.ZERO).add(brukersAndel.getBruttoPrÅr().orElse(BigDecimal.ZERO)))
				.build();
	}

	private FordelAndelModell finnStatusÅFlytteTil(FordelPeriodeModell beregningsgrunnlagPeriode) {
		return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusSomSkalBrukes()
				.stream()
				.filter(a -> !AktivitetStatus.AT.equals(a.getAktivitetStatus()))
				.min(AVKORTING_COMPARATOR)
				.orElseThrow(() -> new IllegalStateException("Forventet å ha en aktivitet å flytte til som ikke er arbeid"));
	}

}
