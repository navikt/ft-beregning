package no.nav.folketrygdloven.kalkulator.modell.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;


public class BeregningRefusjonPeriodeDto {

    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private LocalDate startdatoRefusjon;

    BeregningRefusjonPeriodeDto() {
        // Hibernate
    }

    public BeregningRefusjonPeriodeDto(InternArbeidsforholdRefDto arbeidsforholdRef, LocalDate startdatoRefusjon) {
        Objects.requireNonNull(startdatoRefusjon, "startdatorefusjon");
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.startdatoRefusjon = startdatoRefusjon;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        if (arbeidsforholdRef == null) {
            return InternArbeidsforholdRefDto.nullRef();
        }
        return arbeidsforholdRef;
    }

    public LocalDate getStartdatoRefusjon() {
        return startdatoRefusjon;
    }
}
