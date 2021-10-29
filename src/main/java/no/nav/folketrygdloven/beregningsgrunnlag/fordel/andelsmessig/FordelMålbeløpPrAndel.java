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
import java.util.Map;
import java.util.Optional;

class FordelMålbeløpPrAndel extends LeafSpecification<FordelModell> {
	private static final Comparator<Map.Entry<Inntektskategori, BigDecimal>> TILGJENGELIG_BELØP_COMPARATOR = Comparator.comparingDouble(entry -> entry.getValue().doubleValue());
	private static final Comparator<FordelAndelModellMellomregning> FORESLÅTTBELØP_COMPARATOR = Comparator.comparingDouble(entry -> entry.getInputAndel().getForeslåttPrÅr().orElse(BigDecimal.ZERO).doubleValue());

	static final String ID = "FORDEL_OPP_TIL_MÅLBELØP_PR_ANDEL";
	static final String BESKRIVELSE = "Fordeler brutto til og fra andeler til hver andel har nådd sitt målbeløp";

	public FordelMålbeløpPrAndel() {
		super(ID, BESKRIVELSE);
	}

	@Override
	public Evaluation evaluate(FordelModell modell) {
		Map<String, Object> resultater = new HashMap<>();
		var pottTilFordeling = lagPottTilFordlingFraModell(modell);
		// Fordeler først til andeler med laveste målbeløp
		modell.getMellomregninger().stream()
				.sorted(FORESLÅTTBELØP_COMPARATOR.reversed())
				.forEach(a -> fordelAndel(a, pottTilFordeling, resultater));
		return beregnet(resultater);
	}

	private void fordelAndel(FordelAndelModellMellomregning mellomregning, PottTilFordeling pottTilFordeling, Map<String, Object> resultater) {
		if (mellomregning.getMålbeløp().compareTo(BigDecimal.ZERO) > 0) {
			fordelTilAndel(mellomregning, pottTilFordeling, resultater);
		} else {
			resultater.put("andel", mellomregning.getInputAndel().getBeskrivelse());
			resultater.put("fordelt", BigDecimal.ZERO);
			var fordeltAndel = fordelAndelTil0(mellomregning);
			mellomregning.leggTilFordeltAndel(fordeltAndel);
		}
	}

	private FordelAndelModell fordelAndelTil0(FordelAndelModellMellomregning mellomregning) {
		return opprettAndelFraEksisterende(mellomregning, BigDecimal.ZERO);
	}

	private void fordelTilAndel(FordelAndelModellMellomregning andelÅFordeleTil, PottTilFordeling pottTilFordeling, Map<String, Object> resultater) {
		// Hvis foreslått er satt har andelen brutto fra før, ikke opprettet pga refusjon
		if (andelÅFordeleTil.getInputAndel().getForeslåttPrÅr().isPresent()) {
			fordelTilEksisterendeAndel(andelÅFordeleTil, pottTilFordeling, resultater);
		} else {
			fordelTilAndelUtenBrutto(andelÅFordeleTil, pottTilFordeling, resultater);
		}
	}

	private void fordelTilAndelUtenBrutto(FordelAndelModellMellomregning andelÅFordeleTil, PottTilFordeling pottTilFordeling, Map<String, Object> resultater) {
		var beløpSomGjennstårÅFordele = finnGjenståendeBeløp(andelÅFordeleTil);
		while (beløpSomGjennstårÅFordele.compareTo(BigDecimal.ZERO) > 0) {
			var tilgjengeligBeløpMedKategori = finnBeløpFraBesteKategori(andelÅFordeleTil.getInputAndel().getInntektskategori(), pottTilFordeling)
					.orElseThrow(() -> new IllegalStateException("Har fortsatt mer igjen å fordele, men har ingen andeler med tilgjengelig brutto"));
			var beløpSomSkalFordeles = finnBeløpSomSkalFlyttes(tilgjengeligBeløpMedKategori.getValue(), beløpSomGjennstårÅFordele);
			var inntektskategoriBeløpetTilhørte = tilgjengeligBeløpMedKategori.getKey();
			pottTilFordeling.trekkFraBeløpPåKategori(tilgjengeligBeløpMedKategori.getKey(), beløpSomSkalFordeles);
			flyttBeløpTilAndel(andelÅFordeleTil, beløpSomSkalFordeles, inntektskategoriBeløpetTilhørte);
			settRegelSporing(resultater, andelÅFordeleTil, beløpSomSkalFordeles, inntektskategoriBeløpetTilhørte);
			beløpSomGjennstårÅFordele = beløpSomGjennstårÅFordele.subtract(beløpSomSkalFordeles);
		}
	}

	private Optional<Map.Entry<Inntektskategori, BigDecimal>> finnBeløpFraBesteKategori(Inntektskategori inntektskategori, PottTilFordeling pottTilFordeling) {
		if (pottTilFordeling.finnBeløpForInntektskategori(inntektskategori).isPresent()) {
			return pottTilFordeling.finnBeløpForInntektskategori(inntektskategori);
		} else return pottTilFordeling.finnTilgjengeligBeløpMedInntektskategori();
	}

	private void fordelTilEksisterendeAndel(FordelAndelModellMellomregning andelÅFordeleTil, PottTilFordeling pottTilFordeling, Map<String, Object> resultater) {
		var inntektskategori = andelÅFordeleTil.getInputAndel().getInntektskategori();
		var gjenståendeBeløp = finnGjenståendeBeløp(andelÅFordeleTil);
		pottTilFordeling.trekkFraBeløpPåKategori(inntektskategori, gjenståendeBeløp);
		flyttBeløpTilAndel(andelÅFordeleTil, gjenståendeBeløp , inntektskategori);
		settRegelSporing(resultater, andelÅFordeleTil, gjenståendeBeløp, inntektskategori);
	}

	private void settRegelSporing(Map<String, Object> resultater, FordelAndelModellMellomregning andelÅFordeleTil, BigDecimal beløpSomSkalFordeles, Inntektskategori inntektskategoriBeløpetTilhørte) {
		resultater.put("andel som mottar fordeling", andelÅFordeleTil.getInputAndel().getBeskrivelse());
		resultater.put("beløp som fordeles", beløpSomSkalFordeles);
		resultater.put("inntektskategori beløpet fordeles fra", inntektskategoriBeløpetTilhørte);
	}

	private void flyttBeløpTilAndel(FordelAndelModellMellomregning andelÅFordeleTil,
	                                BigDecimal beløpSomSkalFordeles,
	                                Inntektskategori beløpetsInntektskategori) {
		var alleredeFordeltAndelMedKategori = andelÅFordeleTil.getFordeltAndelMedInntektskategori(beløpetsInntektskategori);
		if (alleredeFordeltAndelMedKategori.isPresent()) {
			var eksisterendeFordeltBeløp = alleredeFordeltAndelMedKategori.get().getFordeltPrÅr().orElseThrow();
			var nyttFordeltBeløp = eksisterendeFordeltBeløp.add(beløpSomSkalFordeles);
			FordelAndelModell.oppdater(alleredeFordeltAndelMedKategori.get()).medFordeltPrÅr(nyttFordeltBeløp);
		} else {
			var fordeltAndel = opprettNyFordeltAndel(andelÅFordeleTil, beløpSomSkalFordeles, beløpetsInntektskategori);
			andelÅFordeleTil.leggTilFordeltAndel(fordeltAndel);
		}
	}

	private FordelAndelModell opprettNyFordeltAndel(FordelAndelModellMellomregning andelÅFordeleTil, BigDecimal beløpSomSkalFordeles, Inntektskategori beløpetsInntektskategori) {
		var erInntektskategoriLikEksisterende = andelÅFordeleTil.getInputAndel().getInntektskategori().equals(beløpetsInntektskategori);
		if (erInntektskategoriLikEksisterende) {
			return opprettAndelFraEksisterende(andelÅFordeleTil, beløpSomSkalFordeles);
		} else {
			return FordelAndelModell.builder()
					.medAktivitetStatus(andelÅFordeleTil.getInputAndel().getAktivitetStatus())
					.medArbeidsforhold(andelÅFordeleTil.getInputAndel().getArbeidsforhold().orElse(null))
					.medFordeltRefusjonPrÅr(beløpSomSkalFordeles)
					.medFordeltPrÅr(beløpSomSkalFordeles)
					.medInntektskategori(beløpetsInntektskategori)
					.erNytt(true)
					.build();
		}
	}

	private BigDecimal finnBeløpSomSkalFlyttes(BigDecimal beløpFraKategori,
	                                           BigDecimal beløpSomGjennstårÅFordele) {
		if (beløpSomGjennstårÅFordele.compareTo(beløpFraKategori) <= 0) {
			return beløpSomGjennstårÅFordele;
		}
		return beløpFraKategori;
	}

	private FordelAndelModell opprettAndelFraEksisterende(FordelAndelModellMellomregning andelÅFordeleTil, BigDecimal fordeltBeløp) {
		return FordelAndelModell.builder()
				.medInntektskategori(andelÅFordeleTil.getInputAndel().getInntektskategori())
				.medArbeidsforhold(andelÅFordeleTil.getInputAndel().getArbeidsforhold().orElse(null))
				.medAktivitetStatus(andelÅFordeleTil.getInputAndel().getAktivitetStatus())
				.medAndelNr(andelÅFordeleTil.getInputAndel().getAndelNr())
				.medFordeltPrÅr(fordeltBeløp)
				.build();
	}

	private BigDecimal finnGjenståendeBeløp(FordelAndelModellMellomregning andelÅFordeleTil) {
		var alleredeFordelt = andelÅFordeleTil.getFordelteAndeler().stream()
				.map(a -> a.getFordeltPrÅr().orElseThrow())
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
		return andelÅFordeleTil.getMålbeløp().subtract(alleredeFordelt);
	}

	private PottTilFordeling lagPottTilFordlingFraModell(FordelModell modell) {
		Map<Inntektskategori, BigDecimal> totalPott = new HashMap<>();
		modell.getMellomregninger().stream()
				.filter(a -> a.getInputAndel().getForeslåttPrÅr().isPresent())
				.forEach(mellomregning -> {
					var andel = mellomregning.getInputAndel();
					var kategori = andel.getInntektskategori();
					BigDecimal eksisterendeBeløp = totalPott.get(kategori);
					if (eksisterendeBeløp == null) {
						totalPott.put(kategori, andel.getForeslåttPrÅr().get());
					} else {
						var nyttBeløp = eksisterendeBeløp.add(andel.getForeslåttPrÅr().get());
						totalPott.put(kategori, nyttBeløp);
					}
				});
		return new PottTilFordeling(totalPott);
	}

	public record PottTilFordeling(Map<Inntektskategori, BigDecimal> kategoriAndelMap) {

		private Optional<Map.Entry<Inntektskategori, BigDecimal>> finnTilgjengeligBeløpMedInntektskategori() {
			// Fordeler først fra andeler med høyeste tilgjengelige beløp
			return kategoriAndelMap.entrySet().stream()
					.filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
					.max(TILGJENGELIG_BELØP_COMPARATOR);
		}

		private Optional<Map.Entry<Inntektskategori, BigDecimal>> finnBeløpForInntektskategori(Inntektskategori inntektskategori) {
			return kategoriAndelMap.entrySet().stream()
					.filter(entry -> entry.getValue().compareTo(BigDecimal.ZERO) > 0)
					.filter(entry ->entry.getKey().equals(inntektskategori))
					.findFirst();
		}

		private void trekkFraBeløpPåKategori(Inntektskategori kategori, BigDecimal beløpÅTrekkeFra) {
			var andel = kategoriAndelMap.entrySet().stream()
					.filter(entry -> entry.getKey().equals(kategori))
					.findFirst()
					.orElseThrow();
			var nåværendeBeløp = andel.getValue();
			if (nåværendeBeløp.compareTo(beløpÅTrekkeFra) < 0) {
				throw new IllegalStateException("Har trukket fra større sum enn tilgjengelig for andel! Fratrukket beløp var "
						+ beløpÅTrekkeFra + " mens beløp tilgjengelig var " + nåværendeBeløp);
			}
			var nyttBeløp = nåværendeBeløp.subtract(beløpÅTrekkeFra);
			kategoriAndelMap.put(kategori, nyttBeløp);
		}
	}
}
