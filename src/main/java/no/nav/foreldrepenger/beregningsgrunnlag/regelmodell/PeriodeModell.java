package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

public class PeriodeModell {
    private List<ArbeidsforholdOgInntektsmelding> arbeidsforholdOgInntektsmeldinger = Collections.emptyList();
    private LocalDate skjæringstidspunkt;
    private BigDecimal grunnbeløp;
    private List<AndelGradering> andelGraderinger = new ArrayList<>();
    private List<SplittetPeriode> eksisterendePerioder = new ArrayList<>();
    private List<PeriodisertBruttoBeregningsgrunnlag> periodisertBruttoBeregningsgrunnlagList = Collections.emptyList();
    private List<AndelGradering> endringerISøktYtelse = new ArrayList<>();

    public List<ArbeidsforholdOgInntektsmelding> getArbeidsforholdOgInntektsmeldinger() {
        return arbeidsforholdOgInntektsmeldinger;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public BigDecimal getGrunnbeløp() {
        return grunnbeløp;
    }

    public List<AndelGradering> getAndelGraderinger() {
        return andelGraderinger;
    }

    public List<SplittetPeriode> getEksisterendePerioder() {
        return eksisterendePerioder;
    }

    public List<PeriodisertBruttoBeregningsgrunnlag> getPeriodisertBruttoBeregningsgrunnlagList() {
        return periodisertBruttoBeregningsgrunnlagList;
    }

    public List<AndelGradering> getEndringerISøktYtelse() {
        return endringerISøktYtelse;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PeriodeModell kladd;

        public Builder() {
            kladd = new PeriodeModell();
        }

        public Builder medSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
            kladd.skjæringstidspunkt = skjæringstidspunkt;
            return this;
        }

        public Builder medGrunnbeløp(BigDecimal grunnbeløp) {
            kladd.grunnbeløp = grunnbeløp;
            return this;
        }

        public Builder medInntektsmeldinger(List<ArbeidsforholdOgInntektsmelding> inntektsmeldinger) {
            kladd.arbeidsforholdOgInntektsmeldinger = inntektsmeldinger;
            return this;
        }

        public Builder medAndelGraderinger(List<AndelGradering> andelGraderinger) {
            kladd.andelGraderinger = andelGraderinger;
            return this;
        }

        public Builder medEndringISøktYtelse(List<AndelGradering> endringISøktYtelse) {
            kladd.endringerISøktYtelse = endringISøktYtelse;
            return this;
        }

        public Builder medEksisterendePerioder(List<SplittetPeriode> eksisterendePerioder) {
            kladd.eksisterendePerioder = eksisterendePerioder;
            return this;
        }

        public Builder medPeriodisertBruttoBeregningsgrunnlag(List<PeriodisertBruttoBeregningsgrunnlag> periodiseringBruttoBg) {
            kladd.periodisertBruttoBeregningsgrunnlagList = periodiseringBruttoBg;
            return this;
        }

        public PeriodeModell build() {
            return kladd;
        }
    }
}
