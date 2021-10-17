package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelAndelModellMellomregning;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

/**
 * Finn fraksjon av totalt foreslått beløp som skal fordeles til hver andel.
 *
 */
class FinnFraksjonPrAndel extends LeafSpecification<FordelModell> {

    static final String ID = "FINN_FRAKSJON_PR_ANDEL";
    static final String BESKRIVELSE = "Bestemmer fraksjon av totalt foreslått beløp eller beløp fra inntektsmeldingen";

    public FinnFraksjonPrAndel() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(FordelModell grunnlag) {
	    Map<String, Object> resultater = new HashMap<>();
	    List<FordelAndelModellMellomregning> mellomregninger = grunnlag.getInput().getAndeler()
			    .stream()
			    .map(andelInput -> finnFraksjonsbestemmendeBeløpOgLagMellomregning(andelInput, resultater))
			    .collect(Collectors.toList());
	    BigDecimal totaltBeløp = finnTotaltFraksjonsbestemmendeBeløp(mellomregninger);
	    resultater.put("totaltFraksjonsbestemmendeBeøp", totaltBeløp);
	    mellomregninger.forEach(mellomregning -> {
			BigDecimal fraksjon = finnFraksjon(mellomregning, totaltBeløp);
			mellomregning.setFraksjonAvBrutto(fraksjon);
			grunnlag.leggTilMellomregningAndel(mellomregning);
			resultater.put("andel", mellomregning.getInputAndel().getBeskrivelse());
			resultater.put("fraksjon", mellomregning.getFraksjonAvBrutto());
		});
	    return beregnet(resultater);
	}

	private BigDecimal finnFraksjon(FordelAndelModellMellomregning mellomregning, BigDecimal totaltBeløp) {
		return mellomregning.getFraksjonsbestemmendeBeløp().divide(totaltBeløp, 10, RoundingMode.HALF_EVEN);
	}

	private BigDecimal finnTotaltFraksjonsbestemmendeBeløp(List<FordelAndelModellMellomregning> mellomregninger) {
		return mellomregninger.stream()
				.map(FordelAndelModellMellomregning::getFraksjonsbestemmendeBeløp)
				.reduce(BigDecimal::add)
				.orElseThrow();
	}

	private FordelAndelModellMellomregning finnFraksjonsbestemmendeBeløpOgLagMellomregning(FordelAndelModell andelInput, Map<String, Object> resultater) {
		BigDecimal fraksjonsbestemmendeBeløp;
		if (kreverRefusjon(andelInput)) {
			fraksjonsbestemmendeBeløp = finnFraksjonsbestemmendeBeløp(andelInput);
		} else {
			fraksjonsbestemmendeBeløp = BigDecimal.ZERO;
		}
		resultater.put("andel", andelInput.getBeskrivelse());
		resultater.put("fraksjonsbestemmende beløp", fraksjonsbestemmendeBeløp);
		return new FordelAndelModellMellomregning(andelInput, fraksjonsbestemmendeBeløp);
	}

	private BigDecimal finnFraksjonsbestemmendeBeløp(FordelAndelModell andelInput) {
		BigDecimal foreslåttEllerIMBeløp;
		if (andelInput.getForeslåttPrÅr().isPresent()) {
			foreslåttEllerIMBeløp = andelInput.getForeslåttPrÅr().get();
		} else {
			BigDecimal beløpFraIM = andelInput.getBeløpFraInntektsMeldingPrMnd().orElseThrow(() -> new IllegalStateException("Mangler både beløp fra inntektsmelding og foreslått brutto, ugyldig tilstand"));
			foreslåttEllerIMBeløp = beløpFraIM.multiply(BigDecimal.valueOf(12));
		}
		BigDecimal refusjonskrav = andelInput.getGjeldendeRefusjonPrÅr().orElseThrow();
		// Siden vi vet at vi ikke har nok brutto til å dekke refusjon når vi fordeler andelsmessig, får arbeidsforholdet aldri mer enn de ber om i refusjon
		return refusjonskrav.compareTo(foreslåttEllerIMBeløp) > 0 ? foreslåttEllerIMBeløp : refusjonskrav;
	}

	private boolean kreverRefusjon(FordelAndelModell andelInput) {
		return andelInput.getGjeldendeRefusjonPrÅr().orElse(BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0;
	}
}
