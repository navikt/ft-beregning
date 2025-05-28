package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelteAndelerModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektskategori;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class FordelMålbeløpPrAndel extends LeafSpecification<FordelModell> {
	private static final Comparator<Map.Entry<Inntektskategori, BigDecimal>> TILGJENGELIG_BELØP_COMPARATOR = Comparator.comparingDouble(entry -> entry.getValue().doubleValue());
	private static final Comparator<FordelteAndelerModell> FORESLÅTTBELØP_COMPARATOR = Comparator.comparingDouble(entry -> entry.getInputAndel().getForeslåttPrÅr().orElse(BigDecimal.ZERO).doubleValue());

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

	private void fordelAndel(FordelteAndelerModell mellomregning, PottTilFordeling pottTilFordeling, Map<String, Object> resultater) {
		if (mellomregning.getMålbeløp().compareTo(BigDecimal.ZERO) > 0) {
			fordelTilAndel(mellomregning, pottTilFordeling, resultater);
		} else {
			resultater.put("andel", mellomregning.getInputAndel().getBeskrivelse());
			resultater.put("fordelt", BigDecimal.ZERO);
			var fordeltAndel = fordelAndelTil0(mellomregning);
			mellomregning.leggTilFordeltAndel(fordeltAndel);
		}
	}

	private FordelAndelModell fordelAndelTil0(FordelteAndelerModell mellomregning) {
		return opprettAndelFraEksisterende(mellomregning, BigDecimal.ZERO, mellomregning.getInputAndel().getInntektskategori());
	}

	private void fordelTilAndel(FordelteAndelerModell andelÅFordeleTil, PottTilFordeling pottTilFordeling, Map<String, Object> resultater) {
		// Hvis foreslått er satt har andelen brutto fra før, ikke opprettet pga refusjon
		if (andelÅFordeleTil.getInputAndel().getGradertForeslåttPrÅr().isPresent()) {
			fordelTilEksisterendeAndel(andelÅFordeleTil, pottTilFordeling, resultater);
		} else {
			fordelTilAndelUtenBrutto(andelÅFordeleTil, pottTilFordeling, resultater);
		}
	}

	private void fordelTilAndelUtenBrutto(FordelteAndelerModell andelÅFordeleTil, PottTilFordeling pottTilFordeling, Map<String, Object> resultater) {
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

	private void fordelTilEksisterendeAndel(FordelteAndelerModell andelÅFordeleTil, PottTilFordeling pottTilFordeling, Map<String, Object> resultater) {
		var inntektskategori = andelÅFordeleTil.getInputAndel().getInntektskategori();
		var gjenståendeBeløp = finnGjenståendeBeløp(andelÅFordeleTil);
		pottTilFordeling.trekkFraBeløpPåKategori(inntektskategori, gjenståendeBeløp);
		flyttBeløpTilAndel(andelÅFordeleTil, gjenståendeBeløp , inntektskategori);
		settRegelSporing(resultater, andelÅFordeleTil, gjenståendeBeløp, inntektskategori);
	}

	private void settRegelSporing(Map<String, Object> resultater, FordelteAndelerModell andelÅFordeleTil, BigDecimal beløpSomSkalFordeles, Inntektskategori inntektskategoriBeløpetTilhørte) {
		resultater.put("andel som mottar fordeling", andelÅFordeleTil.getInputAndel().getBeskrivelse());
		resultater.put("beløp som fordeles", beløpSomSkalFordeles);
		resultater.put("inntektskategori beløpet fordeles fra", inntektskategoriBeløpetTilhørte);
	}

	private void flyttBeløpTilAndel(FordelteAndelerModell andelÅFordeleTil,
	                                BigDecimal beløpSomSkalFordeles,
	                                Inntektskategori beløpetsInntektskategori) {
		var alleredeFordeltAndelMedKategori = andelÅFordeleTil.getFordeltAndelMedInntektskategori(beløpetsInntektskategori);
		if (alleredeFordeltAndelMedKategori.isPresent()) {
			var eksisterendeFordeltBeløp = alleredeFordeltAndelMedKategori.get().getGradertFordeltPrÅr().orElseThrow();
			var nyttFordeltBeløp = eksisterendeFordeltBeløp.add(beløpSomSkalFordeles);
			FordelAndelModell.oppdater(alleredeFordeltAndelMedKategori.get()).medFordeltPrÅr(skalerOpp(nyttFordeltBeløp, alleredeFordeltAndelMedKategori.get().getUtbetalingsgrad()));
		} else {
			var fordeltAndel = opprettNyFordeltAndel(andelÅFordeleTil, beløpSomSkalFordeles, beløpetsInntektskategori);
			andelÅFordeleTil.leggTilFordeltAndel(fordeltAndel);
		}
	}

	private BigDecimal skalerOpp(BigDecimal nyttFordeltBeløp, BigDecimal utbetalingsgrad) {
		if (nyttFordeltBeløp.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return nyttFordeltBeløp.multiply(BigDecimal.valueOf(100).divide(utbetalingsgrad, 10, RoundingMode.HALF_UP));
	}

	private FordelAndelModell opprettNyFordeltAndel(FordelteAndelerModell andelÅFordeleTil, BigDecimal beløpSomSkalFordeles, Inntektskategori beløpetsInntektskategori) {
		var erInntektskategoriLikEksisterende = andelÅFordeleTil.getInputAndel().getInntektskategori().equals(beløpetsInntektskategori);
		var erFørsteAndelPåUdefinertInntektskategori = andelÅFordeleTil.getInputAndel().getInntektskategori().equals(Inntektskategori.UDEFINERT) && andelÅFordeleTil.getFordelteAndeler().isEmpty();
		if (erInntektskategoriLikEksisterende || erFørsteAndelPåUdefinertInntektskategori) {
			return opprettAndelFraEksisterende(andelÅFordeleTil, beløpSomSkalFordeles, beløpetsInntektskategori);
		} else {
			return FordelAndelModell.builder()
					.medAktivitetStatus(andelÅFordeleTil.getInputAndel().getAktivitetStatus())
					.medArbeidsforhold(andelÅFordeleTil.getInputAndel().getArbeidsforhold().orElse(null))
					.medFordeltRefusjonPrÅr(skalerOpp(beløpSomSkalFordeles, andelÅFordeleTil.getInputAndel().getUtbetalingsgrad()))
					.medFordeltPrÅr(skalerOpp(beløpSomSkalFordeles, andelÅFordeleTil.getInputAndel().getUtbetalingsgrad()))
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

	private FordelAndelModell opprettAndelFraEksisterende(FordelteAndelerModell andelÅFordeleTil, BigDecimal fordeltBeløp, Inntektskategori beløpetsInntektskategori) {
		return FordelAndelModell.builder()
				.medArbeidsforhold(andelÅFordeleTil.getInputAndel().getArbeidsforhold().orElse(null))
				.medAktivitetStatus(andelÅFordeleTil.getInputAndel().getAktivitetStatus())
				.medAndelNr(andelÅFordeleTil.getInputAndel().getAndelNr())
				.medFordeltPrÅr(skalerOpp(fordeltBeløp, andelÅFordeleTil.getInputAndel().getUtbetalingsgrad()))
				.medInntektskategori(beløpetsInntektskategori)
				.build();
	}

	private BigDecimal finnGjenståendeBeløp(FordelteAndelerModell andelÅFordeleTil) {
		var alleredeFordelt = andelÅFordeleTil.getFordelteAndeler().stream()
				.map(a -> a.getGradertFordeltPrÅr().orElseThrow())
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
		return andelÅFordeleTil.getMålbeløp().subtract(alleredeFordelt);
	}

	private PottTilFordeling lagPottTilFordlingFraModell(FordelModell modell) {
        var totalPott = new EnumMap<Inntektskategori, BigDecimal>(Inntektskategori.class);
		modell.getMellomregninger().stream()
				.filter(a -> a.getInputAndel().getGradertForeslåttPrÅr().isPresent())
				.forEach(mellomregning -> {
					var andel = mellomregning.getInputAndel();
					var kategori = andel.getInntektskategori();
                    var eksisterendeBeløp = totalPott.get(kategori);
					if (eksisterendeBeløp == null) {
						totalPott.put(kategori, andel.getGradertForeslåttPrÅr().get());
					} else {
						var nyttBeløp = eksisterendeBeløp.add(andel.getGradertForeslåttPrÅr().get());
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
