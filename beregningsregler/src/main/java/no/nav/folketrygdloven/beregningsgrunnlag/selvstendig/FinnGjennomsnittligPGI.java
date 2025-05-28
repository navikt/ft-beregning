package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;

public class FinnGjennomsnittligPGI {

	private FinnGjennomsnittligPGI() {
		// Skjuler default konstruktør
	}

	private static final String BIDRAG_TIL_BG = "bidragTilBG";

	public static BigDecimal finnGjennomsnittligPGI(LocalDate beregningsperiodeTom,
	                                                List<Grunnbeløp> grunnbeløpsatser,
	                                                Inntektsgrunnlag inntektsgrunnlag,
	                                                BigDecimal grunnbeløp,
	                                                Map<String, Object> resultater) {
        var bidragTilBGSum = BigDecimal.ZERO;
		for (var årSiden = 0; årSiden <= 2; årSiden++) {
            var årstall = beregningsperiodeTom.getYear() - årSiden;
            var gSnitt = snittverdiAvG(grunnbeløpsatser, årstall);
            var treGsnitt = gSnitt.multiply(BigDecimal.valueOf(3));
            var seksGsnitt = gSnitt.multiply(BigDecimal.valueOf(6));
            var tolvGsnitt = gSnitt.multiply(BigDecimal.valueOf(12));
            var pgiÅr = inntektsgrunnlag.getÅrsinntektSigrun(årstall);
			if (pgiÅr.compareTo(seksGsnitt) < 1) {
                var bidragTilBG = pgiÅr.compareTo(BigDecimal.ZERO) != 0 ? pgiÅr.divide(gSnitt, 10, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
				resultater.put(BIDRAG_TIL_BG + årstall, bidragTilBG);
				bidragTilBGSum = bidragTilBGSum.add(bidragTilBG);
			} else if (pgiÅr.compareTo(seksGsnitt) > 0 && pgiÅr.compareTo(tolvGsnitt) < 0) {
                var bidragTilBG = pgiÅr.subtract(seksGsnitt).abs().divide(treGsnitt, 10, RoundingMode.HALF_EVEN).add(BigDecimal.valueOf(6));
				resultater.put(BIDRAG_TIL_BG + årstall, bidragTilBG);
				bidragTilBGSum = bidragTilBGSum.add(bidragTilBG);
			} else {
                var bidragTilBG = BigDecimal.valueOf(8);
				resultater.put(BIDRAG_TIL_BG + årstall, bidragTilBG);
				bidragTilBGSum = bidragTilBGSum.add(bidragTilBG);
			}
		}
		return bidragTilBGSum.compareTo(BigDecimal.ZERO) != 0 ? bidragTilBGSum.divide(BigDecimal.valueOf(3), 10, RoundingMode.HALF_EVEN).multiply(grunnbeløp)
				: BigDecimal.ZERO;
	}

	private static BigDecimal snittverdiAvG(List<Grunnbeløp> satser, int år) {
        var optional = satser.stream().filter(g -> g.getFom().getYear() == år).findFirst();
		if (optional.isPresent()) {
			return BigDecimal.valueOf(optional.get().getGSnitt());
		} else {
			throw new IllegalArgumentException("Kjenner ikke GSnitt-verdi for året " + år);
		}
	}

}
