package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.AktivitetStatusV2;
import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

public class SplittetAndel {
    private AktivitetStatusV2 aktivitetStatus;
    private Arbeidsforhold arbeidsforhold;
    private BigDecimal refusjonskravPrÅr;
    private LocalDate arbeidsperiodeFom;
    private LocalDate arbeidsperiodeTom;

    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

    public BigDecimal getRefusjonskravPrÅr() {
        return refusjonskravPrÅr;
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

        public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            kladd.arbeidsforhold = arbeidsforhold;
            return this;
        }

        public Builder medRefusjonskravPrÅr(BigDecimal refusjonskravPrÅr) {
            kladd.refusjonskravPrÅr = refusjonskravPrÅr;
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
}
