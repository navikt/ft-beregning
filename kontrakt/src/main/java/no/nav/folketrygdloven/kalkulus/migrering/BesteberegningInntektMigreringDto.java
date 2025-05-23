package no.nav.folketrygdloven.kalkulus.migrering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.folketrygdloven.kalkulus.felles.v1.Beløp;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;


public class BesteberegningInntektMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
    private OpptjeningAktivitetType opptjeningAktivitetType;

    @Valid
    private Arbeidsgiver arbeidsgiver;

    @Valid
    private InternArbeidsforholdRefDto arbeidsforholdRef;

    @Valid
    @NotNull
    private Beløp inntekt;

    public BesteberegningInntektMigreringDto() {
    }

    public BesteberegningInntektMigreringDto(OpptjeningAktivitetType opptjeningAktivitetType,
                                             Arbeidsgiver arbeidsgiver,
                                             InternArbeidsforholdRefDto arbeidsforholdRef,
                                             Beløp inntekt) {
        this.opptjeningAktivitetType = opptjeningAktivitetType;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.inntekt = inntekt;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public Beløp getInntekt() {
        return inntekt;
    }
}
