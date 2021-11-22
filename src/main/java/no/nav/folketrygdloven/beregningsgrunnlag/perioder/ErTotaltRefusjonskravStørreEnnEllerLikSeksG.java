package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;

class ErTotaltRefusjonskravStørreEnnEllerLikSeksG {
    private ErTotaltRefusjonskravStørreEnnEllerLikSeksG() {
        // skjul public constructor
    }

    public static boolean vurder(PeriodeModell input, LocalDate dato) {
        BigDecimal grunnbeløp = input.getGrunnbeløp();
        BigDecimal seksG = grunnbeløp.multiply(BigDecimal.valueOf(6));
        BigDecimal totaltRefusjonskravPrÅr = beregnTotaltRefusjonskravPrÅrPåDato(input.getPeriodisertBruttoBeregningsgrunnlagList(), dato);
        return totaltRefusjonskravPrÅr.compareTo(seksG) >= 0;
    }

    private static BigDecimal beregnTotaltRefusjonskravPrÅrPåDato(List<PeriodisertBruttoBeregningsgrunnlag> grunnlag, LocalDate dato) {
        BigDecimal årsbeløp = grunnlag.stream()
		        .filter(p -> p.getPeriode().inneholder(dato))
                .flatMap(p -> p.getBruttoBeregningsgrunnlag().stream())
		        .map(BruttoBeregningsgrunnlag::getRefusjonPrÅr)
		        .filter(Objects::nonNull)
		        .reduce(BigDecimal.ZERO, BigDecimal::add);
        return årsbeløp;
    }
}
