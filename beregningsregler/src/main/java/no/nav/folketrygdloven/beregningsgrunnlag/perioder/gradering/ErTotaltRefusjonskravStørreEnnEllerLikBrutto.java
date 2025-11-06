package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

class ErTotaltRefusjonskravStørreEnnEllerLikBrutto {

    private ErTotaltRefusjonskravStørreEnnEllerLikBrutto() {
        // skjul public constructor
    }

    public static boolean vurder(List<PeriodisertBruttoBeregningsgrunnlag> periodisertBeregningsgrunnlag, LocalDate dato) {
        var totaltRefusjonskravPrÅr = beregnTotaltRefusjonskravPrÅrPåDato(periodisertBeregningsgrunnlag, dato);
        var totalBruttoPrÅr = beregnTotalBruttoPrÅr(periodisertBeregningsgrunnlag, dato);
        return totaltRefusjonskravPrÅr.compareTo(totalBruttoPrÅr) >= 0;
    }

    private static BigDecimal beregnTotalBruttoPrÅr(List<PeriodisertBruttoBeregningsgrunnlag> grunnlag, LocalDate dato) {
        return grunnlag.stream()
            .filter(p -> p.getPeriode().inneholder(dato))
            .flatMap(p -> p.getBruttoBeregningsgrunnlag().stream())
            .map(BruttoBeregningsgrunnlag::getBruttoPrÅr)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
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
