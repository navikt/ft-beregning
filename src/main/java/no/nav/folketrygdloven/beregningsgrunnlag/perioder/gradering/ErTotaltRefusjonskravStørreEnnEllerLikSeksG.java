package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.Refusjon;

class ErTotaltRefusjonskravStørreEnnEllerLikSeksG {
    private ErTotaltRefusjonskravStørreEnnEllerLikSeksG() {
        // skjul public constructor
    }

    public static boolean vurder(PeriodeModellGradering input, LocalDate dato) {
        BigDecimal grunnbeløp = input.getGrunnbeløp();
        BigDecimal seksG = grunnbeløp.multiply(BigDecimal.valueOf(6));
        BigDecimal totaltRefusjonskravPrÅr = beregnTotaltRefusjonskravPrÅrPåDato(input.getRefusjoner(), dato);
        return totaltRefusjonskravPrÅr.compareTo(seksG) >= 0;
    }

    private static BigDecimal beregnTotaltRefusjonskravPrÅrPåDato(List<Refusjon> refusjoner, LocalDate dato) {
        BigDecimal årsbeløp = refusjoner.stream()
            .filter(refusjon -> refusjon.getPeriode().inneholder(dato))
            .map(Refusjon::getBeløpPrÅr)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return årsbeløp;
    }
}
