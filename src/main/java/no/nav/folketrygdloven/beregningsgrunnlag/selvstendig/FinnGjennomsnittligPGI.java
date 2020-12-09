package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.Grunnbeløp;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Inntektsgrunnlag;

public class FinnGjennomsnittligPGI {

	private static final String BIDRAG_TIL_BG = "bidragTilBG";

	public static BigDecimal finnGjennomsnittligPGI(LocalDate beregningsperiodeTom,
	                                                List<Grunnbeløp> grunnbeløpsatser,
	                                                Inntektsgrunnlag inntektsgrunnlag,
	                                                BigDecimal grunnbeløp,
	                                                Map<String, Object> resultater) {
		BigDecimal bidragTilBGSum = BigDecimal.ZERO;
		for (int årSiden = 0; årSiden <= 2; årSiden++) {
			int årstall = beregningsperiodeTom.getYear() - årSiden;
			BigDecimal gSnitt = snittverdiAvG(grunnbeløpsatser, årstall);
			BigDecimal treGsnitt = gSnitt.multiply(BigDecimal.valueOf(3));
			BigDecimal seksGsnitt = gSnitt.multiply(BigDecimal.valueOf(6));
			BigDecimal tolvGsnitt = gSnitt.multiply(BigDecimal.valueOf(12));
			BigDecimal pgiÅr = inntektsgrunnlag.getÅrsinntektSigrun(årstall);
			if (pgiÅr.compareTo(seksGsnitt) < 1) {
				BigDecimal bidragTilBG = pgiÅr.compareTo(BigDecimal.ZERO) != 0 ? pgiÅr.divide(gSnitt, 10, RoundingMode.HALF_EVEN) : BigDecimal.ZERO;
				resultater.put(BIDRAG_TIL_BG + årstall, bidragTilBG);
				bidragTilBGSum = bidragTilBGSum.add(bidragTilBG);
			} else if (pgiÅr.compareTo(seksGsnitt) > 0 && pgiÅr.compareTo(tolvGsnitt) < 0) {
				BigDecimal bidragTilBG = pgiÅr.subtract(seksGsnitt).abs().divide(treGsnitt, 10, RoundingMode.HALF_EVEN).add(BigDecimal.valueOf(6));
				resultater.put(BIDRAG_TIL_BG + årstall, bidragTilBG);
				bidragTilBGSum = bidragTilBGSum.add(bidragTilBG);
			} else {
				BigDecimal bidragTilBG = BigDecimal.valueOf(8);
				resultater.put(BIDRAG_TIL_BG + årstall, bidragTilBG);
				bidragTilBGSum = bidragTilBGSum.add(bidragTilBG);
			}
		}
		return bidragTilBGSum.compareTo(BigDecimal.ZERO) != 0 ? bidragTilBGSum.divide(BigDecimal.valueOf(3), 10, RoundingMode.HALF_EVEN).multiply(grunnbeløp)
				: BigDecimal.ZERO;
	}

	private static BigDecimal snittverdiAvG(List<Grunnbeløp> satser, int år) {
		Optional<Grunnbeløp> optional = satser.stream().filter(g -> g.getFom().getYear() == år).findFirst();
		if (optional.isPresent()) {
			return BigDecimal.valueOf(optional.get().getGSnitt());
		} else {
			throw new IllegalArgumentException("Kjenner ikke GSnitt-verdi for året " + år);
		}
	}

}
