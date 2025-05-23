package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;

class ErbruttoinntektForGradertAndelLikNull {

    private ErbruttoinntektForGradertAndelLikNull(){
    }

    public static boolean vurder(PeriodeModellGradering input, AndelGradering andelGradering, LocalDate dato){
        for(var periodisertGrunnlag : input.getPeriodisertBruttoBeregningsgrunnlagList()){
            if(periodisertGrunnlag.getPeriode().inneholder(dato)){
                var grunnlag = finnBruttoBeregningsgrunnlagForGradering(periodisertGrunnlag.getBruttoBeregningsgrunnlag(), andelGradering);
                if(grunnlag.isPresent()){
                    return grunnlag.get().getBruttoPrÅr().equals(BigDecimal.ZERO);
                } else{
                    throw new IllegalStateException("Finner ikke matchende bruttoberegningsgrunnlag for andel som skal graderes " + andelGradering);
                }
            }
        }
        return false;
    }

    private static Optional<BruttoBeregningsgrunnlag> finnBruttoBeregningsgrunnlagForGradering(List<BruttoBeregningsgrunnlag> beregningsgrunnlag,
                                                                                               AndelGradering andelGradering){
        if(andelErSnEllerFl(andelGradering)){
            return beregningsgrunnlag.stream()
                .filter(b -> b.getAktivitetStatus().equals(andelGradering.getAktivitetStatus()))
                .findAny();
        }

        return andelGradering.getArbeidsforhold() == null ? Optional.empty() : finnMatchendeBruttoBeregningsgrunnlagForArbeidsforhold(beregningsgrunnlag, andelGradering.getArbeidsforhold());
    }

    private static Optional<BruttoBeregningsgrunnlag> finnMatchendeBruttoBeregningsgrunnlagForArbeidsforhold(List<BruttoBeregningsgrunnlag> beregningsgrunnlag, Arbeidsforhold arbeidsforhold){
        return beregningsgrunnlag.stream()
            .filter(b -> b.getArbeidsforhold().map(a -> a.equals(arbeidsforhold)).orElse(false))
            .findFirst();
    }

    private static boolean andelErSnEllerFl(AndelGradering andelGradering) {
        return AktivitetStatusV2.FL.equals(andelGradering.getAktivitetStatus()) || AktivitetStatusV2.SN.equals(andelGradering.getAktivitetStatus());
    }
}
