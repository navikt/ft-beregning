package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PeriodisertBruttoBeregningsgrunnlag {
    private Periode periode;
    private List<BruttoBeregningsgrunnlag> bruttoBeregningsgrunnlag = new ArrayList<>();

    private PeriodisertBruttoBeregningsgrunnlag() {
        // skjul default
    }

    public Periode getPeriode() {
        return periode;
    }

    public List<BruttoBeregningsgrunnlag> getBruttoBeregningsgrunnlag() {
        return bruttoBeregningsgrunnlag;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PeriodisertBruttoBeregningsgrunnlag kladd;

        private Builder() {
            kladd = new PeriodisertBruttoBeregningsgrunnlag();
        }

        public Builder medPeriode(Periode periode) {
            kladd.periode = periode;
            return this;
        }

        public Builder leggTilBruttoBeregningsgrunnlag(BruttoBeregningsgrunnlag bruttoBeregningsgrunnlag) {
            kladd.bruttoBeregningsgrunnlag.add(bruttoBeregningsgrunnlag);
            return this;
        }

        public PeriodisertBruttoBeregningsgrunnlag build() {
            verifyStateForBuild();
            return kladd;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(kladd.periode);
            Objects.requireNonNull(kladd.periode.getFom());
        }
    }
}
