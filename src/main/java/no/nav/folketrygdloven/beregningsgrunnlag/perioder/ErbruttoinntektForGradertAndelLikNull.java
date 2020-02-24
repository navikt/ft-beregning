package no.nav.folketrygdloven.beregningsgrunnlag.perioder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeModell;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

class ErbruttoinntektForGradertAndelLikNull {

    private ErbruttoinntektForGradertAndelLikNull(){
    }

    public static boolean vurder(PeriodeModell input, AndelGradering andelGradering, LocalDate dato){
        for(PeriodisertBruttoBeregningsgrunnlag periodisertGrunnlag : input.getPeriodisertBruttoBeregningsgrunnlagList()){
            if(periodisertGrunnlag.getPeriode().inneholder(dato)){
                Optional<BruttoBeregningsgrunnlag> grunnlag = finnBruttoBeregningsgrunnlagForGradering(periodisertGrunnlag.getBruttoBeregningsgrunnlag(), andelGradering, input);
                if(grunnlag.isPresent()){
                    return grunnlag.get().getBruttoBeregningsgrunnlag().equals(BigDecimal.ZERO);
                } else{
                    throw new IllegalStateException("Finner ikke matchende bruttoberegningsgrunnlag for andel som skal graderes");
                }
            }
        }
        return false;
    }

    private static Optional<BruttoBeregningsgrunnlag> finnBruttoBeregningsgrunnlagForGradering(List<BruttoBeregningsgrunnlag> beregningsgrunnlag, AndelGradering andelGradering, PeriodeModell input){
        if(andelErSnEllerFl(andelGradering)){
            return beregningsgrunnlag.stream()
                .filter(b -> b.getAktivitetStatus().equals(andelGradering.getAktivitetStatus()))
                .findAny();
        }

        Optional<ArbeidsforholdOgInntektsmelding> arbeidsforhold = input.getArbeidsforholdOgInntektsmeldinger().stream()
            .filter(a -> a.getGraderinger().size() > 0)
            .filter(a -> matcherArbeidsforholdMedGradering(a, andelGradering))
            .findFirst();

        return arbeidsforhold.isEmpty() ? Optional.empty() : finnMatchendeBruttoBeregningsgrunnlagForArbeidsforhold(beregningsgrunnlag, arbeidsforhold.get().getArbeidsforhold());
    }

    private static boolean matcherArbeidsforholdMedGradering(ArbeidsforholdOgInntektsmelding arbeidsforholdOgInntektsmelding, AndelGradering andelGradering){
        if(andelGradering.getArbeidsforhold().getAktørId() != null) {
            return arbeidsforholdOgInntektsmelding.getArbeidsforhold().getAktørId().equals(andelGradering.getArbeidsforhold().getAktørId());
        }
        return arbeidsforholdOgInntektsmelding.getArbeidsforhold().getOrgnr().equals(andelGradering.getArbeidsforhold().getOrgnr());
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
