package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAktivitetHandlingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;

public class BeregningAktivitetOverstyringDto {

    private Intervall periode;
    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private BeregningAktivitetHandlingType handlingType;
    private OpptjeningAktivitetType opptjeningAktivitetType;
    private BeregningAktivitetOverstyringerDto overstyringerDto;

    public BeregningAktivitetHandlingType getHandling() {
        return handlingType;
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef == null ? InternArbeidsforholdRefDto.nullRef() : arbeidsforholdRef;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }

    public Intervall getPeriode() {
        return periode;
    }

    public BeregningAktivitetNøkkel getNøkkel() {
        return BeregningAktivitetNøkkel.builder()
                .medArbeidsgiverIdentifikator(getArbeidsgiver().map(Arbeidsgiver::getIdentifikator).orElse(null))
                .medArbeidsforholdRef(arbeidsforholdRef != null ? arbeidsforholdRef.getReferanse() : null)
                .medOpptjeningAktivitetType(opptjeningAktivitetType)
                .medFom(periode.getFomDato())
                .build();
    }

    void setBeregningAktivitetOverstyringer(BeregningAktivitetOverstyringerDto overstyringerEntitet) {
        this.overstyringerDto = overstyringerEntitet;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BeregningAktivitetOverstyringDto kladd;

        private Builder() {
            kladd = new BeregningAktivitetOverstyringDto();
        }

        public Builder medPeriode(Intervall periode) {
            kladd.periode = periode;
            return this;
        }

        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            kladd.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medArbeidsforholdRef(InternArbeidsforholdRefDto arbeidsforholdRef) {
            kladd.arbeidsforholdRef = arbeidsforholdRef;
            return this;
        }

        public Builder medOpptjeningAktivitetType(OpptjeningAktivitetType opptjeningAktivitetType) {
            kladd.opptjeningAktivitetType = opptjeningAktivitetType;
            return this;
        }

        public Builder medHandling(BeregningAktivitetHandlingType beregningAktivitetHandlingType) {
            kladd.handlingType = beregningAktivitetHandlingType;
            return this;
        }

        public BeregningAktivitetOverstyringDto build() {
            return kladd;
        }
    }
}
