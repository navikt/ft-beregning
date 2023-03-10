package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelteAndelerModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelModell;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class FinnFraksjonPrAndel extends LeafSpecification<FordelModell> {

	private static final BigDecimal AKSEPTERT_AVVIK = BigDecimal.valueOf(0.0000001);
    static final String ID = "FINN_FRAKSJON_PR_ANDEL";
    static final String BESKRIVELSE = "Bestemmer fraksjon av totalt foreslått beløp eller beløp fra inntektsmeldingen";

    public FinnFraksjonPrAndel() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(FordelModell modell) {
	    Map<String, Object> resultater = new HashMap<>();
	    List<FordelteAndelerModell> mellomregninger = modell.getInput().getAndeler()
			    .stream()
			    .map(andelInput -> finnFraksjonsbestemmendeBeløpOgLagMellomregning(andelInput, resultater))
			    .toList();
	    BigDecimal totaltBeløp = finnTotaltFraksjonsbestemmendeBeløp(mellomregninger);
	    resultater.put("totaltFraksjonsbestemmendeBeløp", totaltBeløp);
		BigDecimal gjennståendeFraksjon = BigDecimal.valueOf(1);
		for (FordelteAndelerModell mellomregning : mellomregninger) {
			BigDecimal utregnetFraksjon = finnFraksjon(mellomregning, totaltBeløp);
			BigDecimal bruktFraksjon = utregnetFraksjon.min(gjennståendeFraksjon);
			gjennståendeFraksjon = gjennståendeFraksjon.subtract(bruktFraksjon);
			mellomregning.setFraksjonAvBrutto(bruktFraksjon);
		}

		// Hvis vi ikke har klart å dele hele fraksjonen (100%) på alle andelene, deler vi ut rest her.
	    // Dette kan skje f.eks ved 3 andeler der alle har krav på like stor fraksjon.
		if (harGjennståendeFraksjonSomIkkeOvergårAkseptertAvvik(gjennståendeFraksjon)) {
			var andelMedKravPåStørsteFraksjon = mellomregninger.stream()
					.max(Comparator.comparing(FordelteAndelerModell::getFraksjonAvBrutto))
					.orElseThrow();
			BigDecimal nyFraksjon = andelMedKravPåStørsteFraksjon.getFraksjonAvBrutto().add(gjennståendeFraksjon);
			andelMedKravPåStørsteFraksjon.setFraksjonAvBrutto(nyFraksjon);
		}

	    validerFraksjoner(mellomregninger);
		mellomregninger.forEach(mellomregning -> {
			modell.leggTilMellomregningAndel(mellomregning);
			resultater.put("andel", mellomregning.getInputAndel().getBeskrivelse());
			resultater.put("fraksjon", mellomregning.getFraksjonAvBrutto());
		});

	    return beregnet(resultater);
	}

	private boolean harGjennståendeFraksjonSomIkkeOvergårAkseptertAvvik(BigDecimal gjennståendeFraksjon) {
		if (gjennståendeFraksjon.compareTo(AKSEPTERT_AVVIK) > 0) {
			throw new IllegalStateException("Feil under fordeling, gjennstående fraksjon å fordele var " + gjennståendeFraksjon);
		}
		return gjennståendeFraksjon.compareTo(BigDecimal.ZERO) > 0;
	}

	private void validerFraksjoner(List<FordelteAndelerModell> mellomregninger) {
		var totalFraksjon = mellomregninger.stream()
				.map(FordelteAndelerModell::getFraksjonAvBrutto)
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
		var avvikIFraksjon = BigDecimal.valueOf(1).subtract(totalFraksjon).abs();
		if (avvikIFraksjon.compareTo(AKSEPTERT_AVVIK) > 0) {
			throw new IllegalStateException("Feil under fordeling, total fraksjons av brutto var " + totalFraksjon);
		}
	}

	private BigDecimal finnFraksjon(FordelteAndelerModell mellomregning, BigDecimal totaltBeløp) {
		return mellomregning.getFraksjonsbestemmendeBeløp().divide(totaltBeløp, 10, RoundingMode.HALF_EVEN);
	}

	private BigDecimal finnTotaltFraksjonsbestemmendeBeløp(List<FordelteAndelerModell> mellomregninger) {
		return mellomregninger.stream()
				.map(FordelteAndelerModell::getFraksjonsbestemmendeBeløp)
				.reduce(BigDecimal::add)
				.orElseThrow();
	}

	private FordelteAndelerModell finnFraksjonsbestemmendeBeløpOgLagMellomregning(FordelAndelModell andelInput, Map<String, Object> resultater) {
		BigDecimal fraksjonsbestemmendeBeløp;
		if (kreverRefusjon(andelInput)) {
			fraksjonsbestemmendeBeløp = andelInput.getGradertRefusjonPrÅr().orElseThrow();
		} else {
			fraksjonsbestemmendeBeløp = BigDecimal.ZERO;
		}
		resultater.put("andel", andelInput.getBeskrivelse());
		resultater.put("fraksjonsbestemmende beløp", fraksjonsbestemmendeBeløp);
		return new FordelteAndelerModell(andelInput, fraksjonsbestemmendeBeløp);
	}

	private boolean kreverRefusjon(FordelAndelModell andelInput) {
		return andelInput.getGradertRefusjonPrÅr().orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0;
	}
}
