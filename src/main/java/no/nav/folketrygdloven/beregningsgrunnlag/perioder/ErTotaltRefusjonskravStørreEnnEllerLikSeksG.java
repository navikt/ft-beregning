package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodeModell;

class ErTotaltRefusjonskravStørreEnnEllerLikSeksG {
    private ErTotaltRefusjonskravStørreEnnEllerLikSeksG() {
        // skjul public constructor
    }

    public static boolean vurder(PeriodeModell input, LocalDate dato) {
        BigDecimal grunnbeløp = input.getGrunnbeløp();
        BigDecimal seksG = grunnbeløp.multiply(BigDecimal.valueOf(6));
        BigDecimal totaltRefusjonskravPrÅr = beregnTotaltRefusjonskravPrÅrPåDato(input.getArbeidsforholdOgInntektsmeldinger(), dato);
        return totaltRefusjonskravPrÅr.compareTo(seksG) >= 0;
    }

    private static BigDecimal beregnTotaltRefusjonskravPrÅrPåDato(List<ArbeidsforholdOgInntektsmelding> inntektsmeldinger, LocalDate dato) {
        BigDecimal årsbeløp = inntektsmeldinger.stream()
            .flatMap(im -> im.getGyldigeRefusjonskrav().stream())
            .filter(refusjon -> refusjon.getPeriode().inneholder(dato))
            .map(refusjonskrav -> refusjonskrav.getMånedsbeløp().multiply(BigDecimal.valueOf(12)))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return årsbeløp;
    }
}
