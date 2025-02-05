package no.nav.folketrygdloven.kalkulus.migrering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.felles.v1.Periode;
import no.nav.folketrygdloven.kalkulus.kodeverk.OpptjeningAktivitetType;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

public class BeregningAktivitetMigreringDto extends BaseMigreringDto {

    @Valid @NotNull
    private Periode periode;

    private Arbeidsgiver arbeidsgiver;

    private InternArbeidsforholdRefDto arbeidsforholdRef;

    @Valid @NotNull
    private OpptjeningAktivitetType opptjeningAktivitetType;

    public BeregningAktivitetMigreringDto() {
    }

    public BeregningAktivitetMigreringDto(Periode periode,
                                          Arbeidsgiver arbeidsgiver,
                                          InternArbeidsforholdRefDto arbeidsforholdRef,
                                          OpptjeningAktivitetType opptjeningAktivitetType) {
        this.periode = periode;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
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

    public OpptjeningAktivitetType getOpptjeningAktivitetType() {
        return opptjeningAktivitetType;
    }
}
