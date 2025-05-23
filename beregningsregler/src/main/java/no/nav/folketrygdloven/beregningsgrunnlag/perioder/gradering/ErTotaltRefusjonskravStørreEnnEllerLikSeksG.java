package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;

class ErTotaltRefusjonskravStørreEnnEllerLikSeksG {
    private ErTotaltRefusjonskravStørreEnnEllerLikSeksG() {
        // skjul public constructor
    }

    public static boolean vurder(PeriodeModellGradering input, LocalDate dato) {
        var grunnbeløp = input.getGrunnbeløp();
        var seksG = grunnbeløp.multiply(BigDecimal.valueOf(6));
        var totaltRefusjonskravPrÅr = beregnTotaltRefusjonskravPrÅrPåDato(input.getPeriodisertBruttoBeregningsgrunnlagList(), dato);
        return totaltRefusjonskravPrÅr.compareTo(seksG) >= 0;
    }

    private static BigDecimal beregnTotaltRefusjonskravPrÅrPåDato(List<PeriodisertBruttoBeregningsgrunnlag> grunnlag, LocalDate dato) {
	    return grunnlag.stream()
		        .filter(p -> p.getPeriode().inneholder(dato))
                .flatMap(p -> p.getBruttoBeregningsgrunnlag().stream())
		        .map(BruttoBeregningsgrunnlag::getRefusjonPrÅr)
		        .filter(Objects::nonNull)
		        .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
