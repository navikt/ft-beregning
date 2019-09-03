package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell;

import java.math.BigDecimal;
import java.util.Objects;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

public class BruttoBeregningsgrunnlag {
    private AktivitetStatusV2 aktivitetStatus;
    private Arbeidsforhold arbeidsforhold;
    private BigDecimal bruttoBeregningsgrunnlag;

    private BruttoBeregningsgrunnlag() {
        // skjul default
    }

    public BigDecimal getBruttoBeregningsgrunnlag() {
        return bruttoBeregningsgrunnlag;
    }

    public AktivitetStatusV2 getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BruttoBeregningsgrunnlag kladd;

        private Builder() {
            kladd = new BruttoBeregningsgrunnlag();
        }

        public Builder medAktivitetStatus(AktivitetStatusV2 aktivitetStatus) {
            kladd.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforhold = arbeidsforhold;
            return this;
        }

        public Builder medBruttoBeregningsgrunnlag(BigDecimal bruttoBg) {
            kladd.bruttoBeregningsgrunnlag = bruttoBg;
            return this;
        }

        public BruttoBeregningsgrunnlag build() {
            Objects.requireNonNull(kladd.getAktivitetStatus(), "aktivitetStatus");
            Objects.requireNonNull(kladd.bruttoBeregningsgrunnlag);
            return kladd;
        }
    }
}
