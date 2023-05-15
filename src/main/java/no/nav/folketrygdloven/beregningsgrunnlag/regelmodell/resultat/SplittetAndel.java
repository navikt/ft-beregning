package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.BeregningsgrunnlagHjemmel;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering.refusjon.Utfall;

public class SplittetAndel {
    private AktivitetStatusV2 aktivitetStatus;
    private Arbeidsforhold arbeidsforhold;
    private BigDecimal refusjonskravPrÅr;
	private BigDecimal innvilgetRefusjonskravPrÅr;
	private Utfall refusjonskravFristUtfall;
    private LocalDate arbeidsperiodeFom;
    private LocalDate arbeidsperiodeTom;
    private BeregningsgrunnlagHjemmel anvendtRefusjonskravfristHjemmel;

    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

    public BigDecimal getRefusjonskravPrÅr() {
        return refusjonskravPrÅr;
    }

	public BigDecimal getInnvilgetRefusjonskravPrÅr() {
		return innvilgetRefusjonskravPrÅr;
	}

	public LocalDate getArbeidsperiodeFom() {
        return arbeidsperiodeFom;
    }

    public LocalDate getArbeidsperiodeTom() {
        return arbeidsperiodeTom;
    }

    public AktivitetStatusV2 getAktivitetStatus() {
        return aktivitetStatus;
    }

    public BeregningsgrunnlagHjemmel getAnvendtRefusjonskravfristHjemmel() {
        return anvendtRefusjonskravfristHjemmel;
    }

	public Utfall getRefusjonskravFristUtfall() {
		return refusjonskravFristUtfall;
	}

	public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SplittetAndel kladd;

        private Builder() {
            kladd = new SplittetAndel();
        }

        public Builder medAktivitetstatus(AktivitetStatusV2 aktivitetStatus) {
            kladd.aktivitetStatus = aktivitetStatus;
            return this;
        }

        public Builder medAnvendtRefusjonskravfristHjemmel(BeregningsgrunnlagHjemmel anvendtRefusjonskravfristHjemmel) {
            kladd.anvendtRefusjonskravfristHjemmel = anvendtRefusjonskravfristHjemmel;
            return this;
        }


        public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforhold = arbeidsforhold;
            return this;
        }

        public Builder medRefusjonskravPrÅr(BigDecimal refusjonskravPrÅr) {
            kladd.refusjonskravPrÅr = refusjonskravPrÅr;
            return this;
        }

	    public Builder medInnvilgetRefusjonskravPrÅr(BigDecimal refusjonskravPrÅr) {
		    kladd.innvilgetRefusjonskravPrÅr = refusjonskravPrÅr;
		    return this;
	    }

	    public Builder medRefusjonskravFristUtfall(Utfall utfall) {
		    kladd.refusjonskravFristUtfall = utfall;
		    return this;
	    }


	    public Builder medArbeidsperiodeFom(LocalDate arbeidsperiodeFom) {
            kladd.arbeidsperiodeFom = arbeidsperiodeFom;
            return this;
        }

        public Builder medArbeidsperiodeTom(LocalDate arbeidsperiodeTom) {
            kladd.arbeidsperiodeTom = arbeidsperiodeTom;
            return this;
        }

        public SplittetAndel build() {
            return kladd;
        }
    }

	@Override
	public String toString() {
		return "SplittetAndel{" +
				"aktivitetStatus=" + aktivitetStatus +
				", arbeidsforhold=" + arbeidsforhold +
				", refusjonskravPrÅr=" + refusjonskravPrÅr +
				", innvilgetRefusjonskravPrÅr=" + innvilgetRefusjonskravPrÅr +
				", refusjonskravFristUtfall=" + refusjonskravFristUtfall +
				", arbeidsperiodeFom=" + arbeidsperiodeFom +
				", arbeidsperiodeTom=" + arbeidsperiodeTom +
				", anvendtRefusjonskravfristHjemmel=" + anvendtRefusjonskravfristHjemmel +
				'}';
	}
}
