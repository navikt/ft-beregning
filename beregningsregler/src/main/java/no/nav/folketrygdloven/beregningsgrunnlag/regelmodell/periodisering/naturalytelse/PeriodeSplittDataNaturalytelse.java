package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.PeriodeÅrsak;

public class PeriodeSplittDataNaturalytelse {

    private LocalDate fom;
    private LocalDate tom;
    private PeriodeÅrsak periodeÅrsak;
	private NaturalytelserPrArbeidsforhold naturalytelserPrArbeidsforhold;

    private PeriodeSplittDataNaturalytelse() {
        //private constructor
    }

    public PeriodeÅrsak getPeriodeÅrsak() {
        return periodeÅrsak;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }


	public NaturalytelserPrArbeidsforhold getNaturalytelserPrArbeidsforhold() {
        return naturalytelserPrArbeidsforhold;
    }


	@Override
	public String toString() {
		return "PeriodeSplittData{" +
				"fom=" + fom +
				", tom=" + tom +
				", periodeÅrsak=" + periodeÅrsak +
				", naturalytelserPrArbeidsforhold=" + naturalytelserPrArbeidsforhold +
				'}';
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (PeriodeSplittDataNaturalytelse) o;
        return Objects.equals(fom, that.fom)
            && Objects.equals(tom, that.tom)
            && Objects.equals(periodeÅrsak, that.periodeÅrsak)
            && Objects.equals(naturalytelserPrArbeidsforhold, that.naturalytelserPrArbeidsforhold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fom, tom, periodeÅrsak, naturalytelserPrArbeidsforhold);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PeriodeSplittDataNaturalytelse kladd;

        private Builder() {
            kladd = new PeriodeSplittDataNaturalytelse();
        }

        public Builder medPeriodeÅrsak(PeriodeÅrsak periodeÅrsak) {
            kladd.periodeÅrsak = periodeÅrsak;
            return this;
        }

        public Builder medFom(LocalDate fom) {
            kladd.fom = fom;
            return this;
        }

        public Builder medNaturalytelserPrArbeidsforhold(NaturalytelserPrArbeidsforhold naturalytelserPrArbeidsforhold) {
            kladd.naturalytelserPrArbeidsforhold = naturalytelserPrArbeidsforhold;
            return this;
        }

        public PeriodeSplittDataNaturalytelse build() {
            Objects.requireNonNull(kladd.periodeÅrsak);
            return kladd;
        }
    }
}
