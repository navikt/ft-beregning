package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModellMellomregning;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sjekker om det finnes et tilkommet arbeidsforhold med refusjonskrav.
 *
 */
class FordelMålbeløpPrAndel extends LeafSpecification<FordelModell> {
	private final Comparator<FordelAndelModellMellomregning> AVKORTING_COMPARATOR = Comparator.comparingInt(a -> a.getInputAndel().getAktivitetStatus().getAvkortingPrioritet());

	static final String ID = "FINN_BELØP_SOM_SKAL_FORDELES_TIL_ANDEL";
	static final String BESKRIVELSE = "Finner beløpet som skal fordeles til andel ved å se på forholdet " +
			"mellom total foreslått brutto og fraksjon av brutto for andelen";

	public FordelMålbeløpPrAndel() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(FordelModell grunnlag) {
		Map<String, Object> resultater = new HashMap<>();
		List<FordelAndelModellMellomregning> mellomregninger = grunnlag.getMellomregninger();
		mellomregninger.forEach(this::opprettAndelerSomIkkeSkalFordeles);
		mellomregninger.forEach(mellomregning -> fordelTilAndel(mellomregning, mellomregninger));
		return beregnet(resultater);
	}

	private void opprettAndelerSomIkkeSkalFordeles(FordelAndelModellMellomregning andelÅFordeleTil) {
		if (andelÅFordeleTil.getBruttoTilgjengeligForFordeling().compareTo(andelÅFordeleTil.getMålbeløp()) >= 0) {
			FordelAndelModell fordeltAndel = FordelAndelModell.builder()
					.medInntektskategori(andelÅFordeleTil.getInputAndel().getInntektskategori())
					.medArbeidsforhold(andelÅFordeleTil.getInputAndel().getArbeidsforhold().orElse(null))
					.medAktivitetStatus(andelÅFordeleTil.getInputAndel().getAktivitetStatus())
					.medAndelNr(andelÅFordeleTil.getInputAndel().getAndelNr())
					.medFordeltPrÅr(andelÅFordeleTil.getBruttoTilgjengeligForFordeling())
					.build();
			andelÅFordeleTil.leggTilFordeltAndel(fordeltAndel);
		}
	}

	private void fordelTilAndel(FordelAndelModellMellomregning andelÅFordeleTil,
	                            List<FordelAndelModellMellomregning> mellomregninger) {
		var beløpSomGjennstårÅFordle = finnGjennståendeBeløp(andelÅFordeleTil);
		var andelSomDetErMuligÅFordeleFra = finnAndelÅFordeleFra(mellomregninger);

		while (beløpSomGjennstårÅFordle.compareTo(BigDecimal.ZERO) > 0 && andelSomDetErMuligÅFordeleFra.isPresent()) {
			BigDecimal beløpSomSkalFlyttes = finnBeløpSomSkalFlyttes(andelSomDetErMuligÅFordeleFra.get(), beløpSomGjennstårÅFordle);
			flyttBeløpFraAndel(beløpSomSkalFlyttes, andelSomDetErMuligÅFordeleFra.get());
			flyttBeløpTilAndel(beløpSomSkalFlyttes, andelÅFordeleTil, andelSomDetErMuligÅFordeleFra.get().getInputAndel().getInntektskategori());
			beløpSomGjennstårÅFordle = beløpSomGjennstårÅFordle.subtract(beløpSomSkalFlyttes);
			andelSomDetErMuligÅFordeleFra = finnAndelÅFordeleFra(mellomregninger);
		}
	}

	private BigDecimal finnGjennståendeBeløp(FordelAndelModellMellomregning andelÅFordeleTil) {
		var alleredeFordelt = andelÅFordeleTil.getFordelteAndeler().stream()
				.map(a -> a.getFordeltPrÅr().orElseThrow())
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
		return andelÅFordeleTil.getMålbeløp().subtract(alleredeFordelt);
	}

	// Inntektskategori må tas med når vi flytter penger, for å markere hva pengene var opptjent som og skal skattes som
	private void flyttBeløpTilAndel(BigDecimal beløpSomSkalFlyttes,
	                                FordelAndelModellMellomregning andelÅFordeleTil,
	                                Inntektskategori inntektskategoriFordeltFra) {
		Optional<FordelAndelModell> fordeltAndel = andelÅFordeleTil.getFordeltAndelMedInntektskategori(inntektskategoriFordeltFra);
		if (fordeltAndel.isPresent()) {
			BigDecimal alleredeFordeltTil = fordeltAndel.get().getFordeltPrÅr().orElseThrow();
			FordelAndelModell.oppdater(fordeltAndel.get())
					.medFordeltPrÅr(alleredeFordeltTil.add(beløpSomSkalFlyttes));
		} else {
			FordelAndelModell nyFordeltAndel;
			if (andelÅFordeleTil.getInputAndel().getInntektskategori().equals(inntektskategoriFordeltFra)) {
				nyFordeltAndel = FordelAndelModell.builder()
						.medInntektskategori(andelÅFordeleTil.getInputAndel().getInntektskategori())
						.medArbeidsforhold(andelÅFordeleTil.getInputAndel().getArbeidsforhold().orElse(null))
						.medAktivitetStatus(andelÅFordeleTil.getInputAndel().getAktivitetStatus())
						.medAndelNr(andelÅFordeleTil.getInputAndel().getAndelNr())
						.medFordeltPrÅr(beløpSomSkalFlyttes)
						.build();
			} else {
				nyFordeltAndel = FordelAndelModell.builder()
						.medArbeidsforhold(andelÅFordeleTil.getInputAndel().getArbeidsforhold().orElse(null))
						.medAktivitetStatus(andelÅFordeleTil.getInputAndel().getAktivitetStatus())
						.medFordeltPrÅr(beløpSomSkalFlyttes)
						.medInntektskategori(inntektskategoriFordeltFra)
						.build();
			}
			andelÅFordeleTil.leggTilFordeltAndel(nyFordeltAndel);
		}
	}

	private void flyttBeløpFraAndel(BigDecimal beløpSomSkalFlyttes, FordelAndelModellMellomregning andelÅFordeleFra) {
		var tilgjengeligBeløpForAndel = andelÅFordeleFra.getBruttoTilgjengeligForFordeling();
		var nyttBeløpForAndel = tilgjengeligBeløpForAndel.subtract(beløpSomSkalFlyttes);
		var fordeltAndel = andelÅFordeleFra.getEnesteFordelteAndel();
		if (fordeltAndel.isEmpty()) {
			var nyFordeltAndel = FordelAndelModell.builder()
					.medInntektskategori(andelÅFordeleFra.getInputAndel().getInntektskategori())
					.medAktivitetStatus(andelÅFordeleFra.getInputAndel().getAktivitetStatus())
					.medArbeidsforhold(andelÅFordeleFra.getInputAndel().getArbeidsforhold().orElse(null))
					.medAndelNr(andelÅFordeleFra.getInputAndel().getAndelNr())
					.medFordeltPrÅr(nyttBeløpForAndel)
					.build();
			andelÅFordeleFra.leggTilFordeltAndel(nyFordeltAndel);
		} else {
			FordelAndelModell.oppdater(fordeltAndel.get()).medFordeltPrÅr(nyttBeløpForAndel);
		}
		andelÅFordeleFra.setBruttoTilgjengeligForFordeling(nyttBeløpForAndel);
	}

	private BigDecimal finnBeløpSomSkalFlyttes(FordelAndelModellMellomregning andelÅFordeleFra,
	                                           BigDecimal beløpSomGjennstårÅFordle) {
		// Kan ikke flytte mer enn andelen selv skal ende opp med
		var beløpSomKanFlyttesFraAndel = andelÅFordeleFra.getBruttoTilgjengeligForFordeling().subtract(andelÅFordeleFra.getMålbeløp());
		return beløpSomGjennstårÅFordle.compareTo(beløpSomKanFlyttesFraAndel) >= 0
				? beløpSomKanFlyttesFraAndel
				: beløpSomGjennstårÅFordle;
	}

	private Optional<FordelAndelModellMellomregning> finnAndelÅFordeleFra(List<FordelAndelModellMellomregning> mellomregninger) {
		return mellomregninger.stream()
				.filter(this::harMerPengerEnnMålbeløp)
				.max(AVKORTING_COMPARATOR);
	}

	private boolean harMerPengerEnnMålbeløp(FordelAndelModellMellomregning andel) {
		return andel.getBruttoTilgjengeligForFordeling().compareTo(andel.getMålbeløp()) > 0;
	}

	@Override
	public String toString() {
		return "FordelMålbeløpPrAndel{" +
				"AVKORTING_COMPARATOR=" + AVKORTING_COMPARATOR +
				'}';
	}
}
