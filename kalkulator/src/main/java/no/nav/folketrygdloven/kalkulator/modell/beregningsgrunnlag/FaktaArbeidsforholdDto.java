package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.FaktaVurdering;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.FaktaVurderingKilde;


public class FaktaArbeidsforholdDto {

    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private FaktaVurdering erTidsbegrenset;
    private FaktaVurdering harMottattYtelse;
    private FaktaVurdering harLønnsendringIBeregningsperioden;

    public FaktaArbeidsforholdDto(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRefDto) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRefDto;
    }

    public FaktaArbeidsforholdDto(FaktaArbeidsforholdDto original) {
        this.arbeidsgiver = Arbeidsgiver.fra(original.getArbeidsgiver());
        this.arbeidsforholdRef = original.getArbeidsforholdRef();
        this.erTidsbegrenset = original.getErTidsbegrenset();
        this.harMottattYtelse = original.getHarMottattYtelse();
        this.harLønnsendringIBeregningsperioden = original.getHarLønnsendringIBeregningsperioden();
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef == null ? InternArbeidsforholdRefDto.nullRef() : arbeidsforholdRef;
    }

    public boolean gjelderFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
        return Objects.equals(this.getArbeidsgiver(), arbeidsgiver) &&
                this.getArbeidsforholdRef().gjelderFor(arbeidsforholdRef);
    }

    public Boolean getErTidsbegrensetVurdering() {
        return finnVurdering(erTidsbegrenset);
    }

    public Boolean getHarMottattYtelseVurdering() {
        return finnVurdering(harMottattYtelse);
    }

    public Boolean getHarLønnsendringIBeregningsperiodenVurdering() {
        return finnVurdering(harLønnsendringIBeregningsperioden);
    }

    private Boolean finnVurdering(FaktaVurdering vurdering) {
        return vurdering == null ? null : vurdering.getVurdering();
    }

    public FaktaVurdering getErTidsbegrenset() {
        return erTidsbegrenset;
    }

    public FaktaVurdering getHarMottattYtelse() {
        return harMottattYtelse;
    }

    public FaktaVurdering getHarLønnsendringIBeregningsperioden() {
        return harLønnsendringIBeregningsperioden;
    }

    @Override
    public String toString() {
        return "FaktaArbeidsforholdDto{" +
                "arbeidsgiver=" + arbeidsgiver +
                ", arbeidsforholdRef=" + arbeidsforholdRef +
                ", erTidsbegrenset=" + erTidsbegrenset +
                ", harMottattYtelse=" + harMottattYtelse +
                ", harLønnsendringIBeregningsperioden=" + harLønnsendringIBeregningsperioden +
                '}';
    }

    public static Builder builder(FaktaArbeidsforholdDto kopi) {
        return new Builder(kopi);
    }

    public static Builder builder(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRefDto) {
        return new Builder(arbeidsgiver, arbeidsforholdRefDto);
    }

    public static class Builder {
        private FaktaArbeidsforholdDto mal;

        public Builder(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef) {
            mal = new FaktaArbeidsforholdDto(arbeidsgiver, arbeidsforholdRef);
        }

        private Builder(FaktaArbeidsforholdDto faktaArbeidsforholdDto) {
            mal = new FaktaArbeidsforholdDto(faktaArbeidsforholdDto);
        }

        static Builder oppdater(FaktaArbeidsforholdDto faktaArbeidsforholdDto) {
            return new Builder(faktaArbeidsforholdDto);
        }

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            mal.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medArbeidsforholdRef(InternArbeidsforholdRefDto arbeidsforholdRef) {
            mal.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medHarMottattYtelse(FaktaVurdering harMottattYtelse) {
            mal.harMottattYtelse = harMottattYtelse;
            return this;
        }

        public Builder medErTidsbegrenset(FaktaVurdering erTidsbegrenset) {
            mal.erTidsbegrenset = erTidsbegrenset;
            return this;
        }

        public Builder medHarLønnsendringIBeregningsperioden(FaktaVurdering harLønnsendringIBeregningsperioden) {
            mal.harLønnsendringIBeregningsperioden = harLønnsendringIBeregningsperioden;
            return this;
        }

        public Builder medHarMottattYtelseFastsattAvSaksbehandler(Boolean harMottattYtelse) {
            mal.harMottattYtelse = harMottattYtelse == null ? null : new FaktaVurdering(harMottattYtelse, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }

        public Builder medErTidsbegrensetFastsattAvSaksbehandler(Boolean erTidsbegrenset) {
            mal.erTidsbegrenset = erTidsbegrenset == null ? null : new FaktaVurdering(erTidsbegrenset, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }

        public Builder medHarLønnsendringIBeregningsperiodenFastsattAvSaksbehandler(Boolean harLønnsendringIBeregningsperioden) {
            mal.harLønnsendringIBeregningsperioden = harLønnsendringIBeregningsperioden == null ? null : new FaktaVurdering(harLønnsendringIBeregningsperioden, FaktaVurderingKilde.SAKSBEHANDLER);
            return this;
        }

        public FaktaArbeidsforholdDto build() {
            verifyStateForBuild();
            return mal;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(mal.arbeidsgiver, "arbeidsgiver");
            if (manglerFakta()) {
                throw new IllegalStateException("Må ha satt minst et faktafelt.");
            }
        }

        // Brukes av fp-sak og må vere public
        public boolean manglerFakta() {
            return mal.erTidsbegrenset == null &&
                    mal.harLønnsendringIBeregningsperioden == null &&
                    mal.harMottattYtelse == null;
        }
    }
}
