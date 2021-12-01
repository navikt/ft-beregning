package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelteAndelerModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

class FinnFraksjonPrAndel extends LeafSpecification<FordelModell> {

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
			    .collect(Collectors.toList());
	    BigDecimal totaltBeløp = finnTotaltFraksjonsbestemmendeBeløp(mellomregninger);
	    resultater.put("totaltFraksjonsbestemmendeBeløp", totaltBeløp);
		BigDecimal gjennståendeFraksjon = BigDecimal.valueOf(1);
		for (FordelteAndelerModell mellomregning : mellomregninger) {
			BigDecimal utregnetFraksjon = finnFraksjon(mellomregning, totaltBeløp);
			BigDecimal bruktFraksjon = utregnetFraksjon.min(gjennståendeFraksjon);
			gjennståendeFraksjon = gjennståendeFraksjon.subtract(bruktFraksjon);
			mellomregning.setFraksjonAvBrutto(bruktFraksjon);
			modell.leggTilMellomregningAndel(mellomregning);
			resultater.put("andel", mellomregning.getInputAndel().getBeskrivelse());
			resultater.put("fraksjon", mellomregning.getFraksjonAvBrutto());
		}
		validerFraksjoner(mellomregninger);
	    return beregnet(resultater);
	}

	private void validerFraksjoner(List<FordelteAndelerModell> mellomregninger) {
		var totalFraksjon = mellomregninger.stream()
				.map(FordelteAndelerModell::getFraksjonAvBrutto)
				.reduce(BigDecimal::add)
				.orElse(BigDecimal.ZERO);
		if (totalFraksjon.compareTo(BigDecimal.valueOf(1)) != 0) {
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
			fraksjonsbestemmendeBeløp = andelInput.getGjeldendeRefusjonPrÅr().orElseThrow();
		} else {
			fraksjonsbestemmendeBeløp = BigDecimal.ZERO;
		}
		resultater.put("andel", andelInput.getBeskrivelse());
		resultater.put("fraksjonsbestemmende beløp", fraksjonsbestemmendeBeløp);
		return new FordelteAndelerModell(andelInput, fraksjonsbestemmendeBeløp);
	}

	private boolean kreverRefusjon(FordelAndelModell andelInput) {
		return andelInput.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0;
	}
}
