package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AndelGradering;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.ArbeidsforholdOgInntektsmelding;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.PeriodisertBruttoBeregningsgrunnlag;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.SplittetPeriode;

public class PeriodeModellNaturalytelse {
    private List<NaturalytelserPrArbeidsforhold> arbeidsforholdOgInntektsmeldinger = Collections.emptyList();
    private LocalDate skjæringstidspunkt;
    private BigDecimal grunnbeløp;
    private List<SplittetPeriode> eksisterendePerioder = new ArrayList<>();

    public List<NaturalytelserPrArbeidsforhold> getNaturalytelserPrArbeidsforhold() {
        return arbeidsforholdOgInntektsmeldinger;
    }

    public LocalDate getSkjæringstidspunkt() {
        return skjæringstidspunkt;
    }

    public BigDecimal getGrunnbeløp() {
        return grunnbeløp;
    }

    public List<SplittetPeriode> getEksisterendePerioder() {
        return eksisterendePerioder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PeriodeModellNaturalytelse kladd;

        public Builder() {
            kladd = new PeriodeModellNaturalytelse();
        }

        public Builder medSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
            kladd.skjæringstidspunkt = skjæringstidspunkt;
            return this;
        }

        public Builder medGrunnbeløp(BigDecimal grunnbeløp) {
            kladd.grunnbeløp = grunnbeløp;
            return this;
        }

        public Builder medInntektsmeldinger(List<NaturalytelserPrArbeidsforhold> inntektsmeldinger) {
            kladd.arbeidsforholdOgInntektsmeldinger = inntektsmeldinger;
            return this;
        }

        public Builder medEksisterendePerioder(List<SplittetPeriode> eksisterendePerioder) {
            kladd.eksisterendePerioder = eksisterendePerioder;
            return this;
        }

        public PeriodeModellNaturalytelse build() {
            return kladd;
        }
    }
}
