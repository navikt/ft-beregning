package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
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
class OmfordelFraBrukersAndel extends LeafSpecification<FordelModell> {

	public static final String ID = "OMFORDEL_FRA_BA";
	public static final String BESKRIVELSE = "Flytt beregningsgrunnlag fra brukers andel til aktivitetstatus med høyere prioritet";
	private final Comparator<FordelAndelModell> AVKORTING_COMPARATOR = Comparator.comparingInt(a -> a.getAktivitetStatus().getAvkortingPrioritet());

	OmfordelFraBrukersAndel() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(FordelModell modell) {
		Map<String, Object> resultater = new HashMap<>();
		var brukersAndel = modell.getInput().getAlleAndelerForStatus(AktivitetStatus.BA);
		var statusSomAvkortesSistUtenArbeid = finnStatusÅFlytteTil(modell.getInput());
		resultater.put("aktivitetstatusSomFlyttesTilFraInaktiv", statusSomAvkortesSistUtenArbeid.getAktivitetStatus());
		flyttTil(brukersAndel, statusSomAvkortesSistUtenArbeid, modell.getInput());
		settBrukersAndelTil0(brukersAndel);
		return beregnet(resultater);
	}

	private void settBrukersAndelTil0(List<FordelAndelModell> brukersAndel) {
		brukersAndel.forEach(ba -> FordelAndelModell.oppdater(ba)
				.medFordeltPrÅr(BigDecimal.ZERO)
				.build());
	}

	private void flyttTil(List<FordelAndelModell> brukersAndel, FordelAndelModell flytteTilAndel, FordelPeriodeModell periodeModell) {
		// Siden vi er her vet vi at det finnes minst ett element i lista, flytter bg herfra til flytteTilAndel
		var førsteBrukersAndel = brukersAndel.get(0);
		FordelAndelModell.oppdater(flytteTilAndel)
				.medInntektskategori(førsteBrukersAndel.getInntektskategori())
				.medFordeltPrÅr(flytteTilAndel.getBruttoPrÅr().orElse(BigDecimal.ZERO).add(førsteBrukersAndel.getBruttoPrÅr().orElse(BigDecimal.ZERO)))
				.build();
		if (brukersAndel.size() > 1) {
			fordelRestenTilNyeAndeler(brukersAndel, flytteTilAndel, periodeModell);
		}
	}

	private void fordelRestenTilNyeAndeler(List<FordelAndelModell> brukersAndel,
	                                       FordelAndelModell eksisterendeAndel,
	                                       FordelPeriodeModell periodeModell) {
		var restenAvLista = brukersAndel.subList(1, brukersAndel.size());
		for (FordelAndelModell andel : restenAvLista) {
			var nyAndel = opprettNyAndelBasertPå(eksisterendeAndel);
			FordelAndelModell.oppdater(nyAndel)
					.medInntektskategori(andel.getInntektskategori())
					.medFordeltPrÅr(andel.getBruttoPrÅr().orElse(BigDecimal.ZERO))
					.build();
			periodeModell.leggTilAndel(nyAndel);
		}
	}

	private FordelAndelModell finnStatusÅFlytteTil(FordelPeriodeModell beregningsgrunnlagPeriode) {
		return beregningsgrunnlagPeriode.getBeregningsgrunnlagPrStatusSomSkalBrukes()
				.stream()
				.filter(a -> !AktivitetStatus.AT.equals(a.getAktivitetStatus()))
				.min(AVKORTING_COMPARATOR)
				.orElseThrow(() -> new IllegalStateException("Forventet å ha en aktivitet å flytte til som ikke er arbeid"));
	}

	private FordelAndelModell opprettNyAndelBasertPå(FordelAndelModell basertPå) {
		return FordelAndelModell.builder()
				.medArbeidsforhold(basertPå.getArbeidsforhold().orElse(null))
				.medAktivitetStatus(basertPå.getAktivitetStatus())
				.erNytt(true)
				.build();
	}


}
