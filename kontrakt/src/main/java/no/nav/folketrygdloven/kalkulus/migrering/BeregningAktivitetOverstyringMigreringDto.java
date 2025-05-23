package no.nav.folketrygdloven.kalkulus.migrering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.BeregningAktivitetHandlingType;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

public class BeregningAktivitetOverstyringMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
    private Periode periode;

    @Valid
    private Arbeidsgiver arbeidsgiver;

    @Valid
    private InternArbeidsforholdRefDto arbeidsforholdRef;

    @Valid
    @NotNull
    private BeregningAktivitetHandlingType handlingType;

    @Valid
    @NotNull
    private OpptjeningAktivitetType opptjeningAktivitetType;

    public BeregningAktivitetOverstyringMigreringDto() {
    }

    public BeregningAktivitetOverstyringMigreringDto(Periode periode,
                                                     Arbeidsgiver arbeidsgiver,
                                                     InternArbeidsforholdRefDto arbeidsforholdRef,
                                                     BeregningAktivitetHandlingType handlingType,
                                                     OpptjeningAktivitetType opptjeningAktivitetType) {
        this.periode = periode;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.handlingType = handlingType;
        this.opptjeningAktivitetType = opptjeningAktivitetType;
    }

    public Periode getPeriode() {
        return periode;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public BeregningAktivitetHandlingType getHandlingType() {
        return handlingType;
    }

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }
}
