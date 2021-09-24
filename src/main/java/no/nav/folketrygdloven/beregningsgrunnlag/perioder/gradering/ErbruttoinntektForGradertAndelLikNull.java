package no.nav.folketrygdloven.beregningsgrunnlag.perioder.gradering;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.GraderingPrAktivitet;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.gradering.PeriodeModellGradering;

class ErbruttoinntektForGradertAndelLikNull {

    private ErbruttoinntektForGradertAndelLikNull(){
    }

    public static boolean vurder(PeriodeModellGradering input, GraderingPrAktivitet gradering, LocalDate dato){
        for(PeriodisertBruttoBeregningsgrunnlag periodisertGrunnlag : input.getPeriodisertBruttoBeregningsgrunnlagList()){
            if(periodisertGrunnlag.getPeriode().inneholder(dato)){
                Optional<BruttoBeregningsgrunnlag> grunnlag = finnBruttoBeregningsgrunnlagForGradering(periodisertGrunnlag.getBruttoBeregningsgrunnlag(), gradering);
                if(grunnlag.isPresent()){
                    return grunnlag.get().getBruttoPrÅr().equals(BigDecimal.ZERO);
                } else{
                    throw new IllegalStateException("Finner ikke matchende bruttoberegningsgrunnlag for andel som skal graderes " + gradering);
                }
            }
        }
        return false;
    }

    private static Optional<BruttoBeregningsgrunnlag> finnBruttoBeregningsgrunnlagForGradering(List<BruttoBeregningsgrunnlag> beregningsgrunnlag,
                                                                                               GraderingPrAktivitet gradering){
        if(andelErSnEllerFl(gradering)){
            return beregningsgrunnlag.stream()
                .filter(b -> b.getAktivitetStatus().equals(gradering.getAktivitetStatus()))
                .findAny();
        }

        return gradering.getArbeidsforhold() == null ? Optional.empty() : finnMatchendeBruttoBeregningsgrunnlagForArbeidsforhold(beregningsgrunnlag, gradering.getArbeidsforhold());
    }


    private static Optional<BruttoBeregningsgrunnlag> finnMatchendeBruttoBeregningsgrunnlagForArbeidsforhold(List<BruttoBeregningsgrunnlag> beregningsgrunnlag, Arbeidsforhold arbeidsforhold){
        return beregningsgrunnlag.stream()
            .filter(b -> b.getArbeidsforhold().map(a -> a.equals(arbeidsforhold)).orElse(false))
            .findFirst();
    }

    private static boolean andelErSnEllerFl(GraderingPrAktivitet gradering) {
        return AktivitetStatusV2.FL.equals(gradering.getAktivitetStatus()) || AktivitetStatusV2.SN.equals(gradering.getAktivitetStatus());
    }
}
