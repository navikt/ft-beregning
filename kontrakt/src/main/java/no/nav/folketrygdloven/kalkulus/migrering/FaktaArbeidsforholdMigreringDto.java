package no.nav.folketrygdloven.kalkulus.migrering;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.response.v1.Arbeidsgiver;

public class FaktaArbeidsforholdMigreringDto extends BaseMigreringDto {

    @Valid
    @NotNull
    private Arbeidsgiver arbeidsgiver;

    @Valid
    private InternArbeidsforholdRefDto arbeidsforholdRef;

    @Valid
    private FaktaVurderingMigreringDto erTidsbegrenset;

    @Valid
    private FaktaVurderingMigreringDto harMottattYtelse;

    @Valid
    private FaktaVurderingMigreringDto harLønnsendringIBeregningsperioden;

    public FaktaArbeidsforholdMigreringDto() {
    }

    public FaktaArbeidsforholdMigreringDto(Arbeidsgiver arbeidsgiver,
                                           InternArbeidsforholdRefDto arbeidsforholdRef,
                                           FaktaVurderingMigreringDto erTidsbegrenset,
                                           FaktaVurderingMigreringDto harMottattYtelse,
                                           FaktaVurderingMigreringDto harLønnsendringIBeregningsperioden) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.erTidsbegrenset = erTidsbegrenset;
        this.harMottattYtelse = harMottattYtelse;
        this.harLønnsendringIBeregningsperioden = harLønnsendringIBeregningsperioden;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public FaktaVurderingMigreringDto getErTidsbegrenset() {
        return erTidsbegrenset;
    }

    public FaktaVurderingMigreringDto getHarMottattYtelse() {
        return harMottattYtelse;
    }

    public FaktaVurderingMigreringDto getHarLønnsendringIBeregningsperioden() {
        return harLønnsendringIBeregningsperioden;
    }
}
