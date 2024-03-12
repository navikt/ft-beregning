package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.naturalytelse;


import java.util.Collections;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.NaturalYtelse;

public class NaturalytelserPrArbeidsforhold {
    private Arbeidsforhold arbeidsforhold;
	private List<NaturalYtelse> naturalYtelser = Collections.emptyList();
    private Long andelsnr;

    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

    public List<NaturalYtelse> getNaturalYtelser() {
        return naturalYtelser;
    }

	public Long getAndelsnr() {
		return andelsnr;
	}

	public boolean erNyAktivitet() {
		return andelsnr == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NaturalytelserPrArbeidsforhold)) return false;
        NaturalytelserPrArbeidsforhold that = (NaturalytelserPrArbeidsforhold) o;
        return Objects.equals(arbeidsforhold, that.arbeidsforhold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsforhold);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(NaturalytelserPrArbeidsforhold arbeidsforholdOgInntektsmelding) {
        return new Builder(arbeidsforholdOgInntektsmelding);
    }

    public static class Builder {
        private final NaturalytelserPrArbeidsforhold kladd;

        private Builder() {
            kladd = new NaturalytelserPrArbeidsforhold();
        }

        private Builder(NaturalytelserPrArbeidsforhold arbeidsforholdOgInntektsmelding) {
            kladd = arbeidsforholdOgInntektsmelding;
        }

	    public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforhold = arbeidsforhold;
            return this;
        }


        public Builder medNaturalytelser(List<NaturalYtelse> naturalYtelser) {
            kladd.naturalYtelser = naturalYtelser;
            return this;
        }


        public Builder medAndelsnr(Long andelsnr) {
            kladd.andelsnr = andelsnr;
            return this;
        }

        public NaturalytelserPrArbeidsforhold build() {
            return kladd;
        }
    }

    @Override
    public String toString() {
        return "NaturalytelserPrArbeidsforhold{" +
            "arbeidsforhold=" + arbeidsforhold +
            ", naturalYtelser=" + naturalYtelser +
            ", andelsnr=" + andelsnr +
            '}';
    }
}
