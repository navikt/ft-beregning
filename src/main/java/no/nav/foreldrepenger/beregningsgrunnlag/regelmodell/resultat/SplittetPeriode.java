package no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Periode;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.PeriodeÅrsak;

public class SplittetPeriode {
    private Periode periode;
    private List<PeriodeÅrsak> periodeÅrsaker;
    private List<BeregningsgrunnlagPrArbeidsforhold> eksisterendePeriodeAndeler;
    private List<SplittetAndel> nyeAndeler = new ArrayList<>();

    public Periode getPeriode() {
        return periode;
    }

    public List<PeriodeÅrsak> getPeriodeÅrsaker() {
        return periodeÅrsaker;
    }

    public List<BeregningsgrunnlagPrArbeidsforhold> getEksisterendePeriodeAndeler() {
        return eksisterendePeriodeAndeler;
    }

    public List<SplittetAndel> getNyeAndeler() {
        return nyeAndeler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SplittetPeriode that = (SplittetPeriode) o;
        return periode.equals(that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SplittetPeriode kladd;

        private Builder() {
            kladd = new SplittetPeriode();
        }

        public Builder medPeriodeÅrsaker(List<PeriodeÅrsak> periodeÅrsaker) {
            kladd.periodeÅrsaker = periodeÅrsaker;
            return this;
        }

        public Builder medPeriode(Periode periode) {
            kladd.periode = periode;
            return this;
        }

        public Builder medFørstePeriodeAndeler(List<BeregningsgrunnlagPrArbeidsforhold> førstePeriodeAndeler) {
            kladd.eksisterendePeriodeAndeler = førstePeriodeAndeler;
            return this;
        }

        public Builder medNyeAndeler(List<SplittetAndel> nyeAndeler) {
            kladd.nyeAndeler = nyeAndeler;
            return this;
        }

        public SplittetPeriode build() {
            return kladd;
        }
    }
}
