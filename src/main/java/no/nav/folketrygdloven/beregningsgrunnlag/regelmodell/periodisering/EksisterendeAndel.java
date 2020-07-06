package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.periodisering;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;

public class EksisterendeAndel {
    private BigDecimal naturalytelseBortfaltPrÅr;
    private BigDecimal naturalytelseTilkommetPrÅr;
    private Arbeidsforhold arbeidsforhold;
    private BigDecimal refusjonskravPrÅr;
    private Long andelNr;
    private boolean harVurdertRefusjonskravfrist;

    public EksisterendeAndel() {
    }

    public String getArbeidsgiverId() {
        return arbeidsforhold.getArbeidsgiverId();
    }


    public boolean erFrilanser() {
        return arbeidsforhold.erFrilanser();
    }

    public Optional<BigDecimal> getNaturalytelseBortfaltPrÅr() {
        return Optional.ofNullable(naturalytelseBortfaltPrÅr);
    }

    public Optional<BigDecimal> getNaturalytelseTilkommetPrÅr() {
        return Optional.ofNullable(naturalytelseTilkommetPrÅr);
    }

    public Arbeidsforhold getArbeidsforhold() {
        return arbeidsforhold;
    }

    public Optional<BigDecimal> getRefusjonskravPrÅr() {
        return Optional.ofNullable(refusjonskravPrÅr);
    }


    public Long getAndelNr() {
        return andelNr;
    }



    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(EksisterendeAndel af) {
        return new Builder(af);
    }
    public static class Builder {

        private EksisterendeAndel mal;
        private boolean erNytt;

        public Builder() {
            mal = new EksisterendeAndel();
        }

        public Builder(EksisterendeAndel af) {
            mal = af;
        }

        public Builder medArbeidsforhold(Arbeidsforhold arbeidsforhold) {
            mal.arbeidsforhold = arbeidsforhold;
            return this;
        }

        public Builder erNytt(boolean erNytt) {
            this.erNytt = erNytt;
            return this;
        }

        public Builder medNaturalytelseBortfaltPrÅr(BigDecimal naturalytelseBortfaltPrÅr) {
            mal.naturalytelseBortfaltPrÅr = naturalytelseBortfaltPrÅr;
            return this;
        }

        public Builder medNaturalytelseTilkommetPrÅr(BigDecimal naturalytelseTilkommetPrÅr) {
            mal.naturalytelseTilkommetPrÅr = naturalytelseTilkommetPrÅr;
            return this;
        }



        public Builder medRefusjonskravPrÅr(BigDecimal refusjonskravPrÅr) {
            mal.refusjonskravPrÅr = refusjonskravPrÅr;
            return this;
        }

        public Builder medAndelNr(long andelNr) {
            mal.andelNr = andelNr;
            return this;
        }

        public Builder medHarVurdertRefusjonskravfrist(boolean harVurdertRefusjonskravfrist) {
            mal.harVurdertRefusjonskravfrist = harVurdertRefusjonskravfrist;
            return this;
        }


        public EksisterendeAndel build() {
            verifyStateForBuild();
            return mal;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(mal.arbeidsforhold, "arbeidsforhold");
            if (!erNytt) {
                Objects.requireNonNull(mal.andelNr, "andelNr");
            }
        }
    }
}
