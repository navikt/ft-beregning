package no.nav.folketrygdloven.kalkulus.migrering;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.folketrygdloven.kalkulus.felles.v1.InternArbeidsforholdRefDto;

public class BeregningRefusjonPeriodeMigreringDto extends BaseMigreringDto {

    @Valid
    private InternArbeidsforholdRefDto arbeidsforholdRef;

    @Valid
    @NotNull
    private LocalDate startdatoRefusjon;

    BeregningRefusjonPeriodeMigreringDto() {
        // Hibernate
    }

    public BeregningRefusjonPeriodeMigreringDto(InternArbeidsforholdRefDto arbeidsforholdRef, LocalDate startdatoRefusjon) {
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.startdatoRefusjon = startdatoRefusjon;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef;
    }

    public LocalDate getStartdatoRefusjon() {
        return startdatoRefusjon;
    }
}
