package no.nav.folketrygdloven.kalkulator.modell.svp;

import java.util.Objects;
import java.util.Optional;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.UttakArbeidType;

public final class AktivitetDto {

    private final Arbeidsgiver arbeidsgiver;
    private final InternArbeidsforholdRefDto internArbeidsforholdRef;
    private final UttakArbeidType uttakArbeidType;
    private boolean arbeidsforholdErSplittet = false;

    public AktivitetDto(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto internArbeidsforholdRef, UttakArbeidType uttakArbeidType) {
        this.arbeidsgiver = arbeidsgiver;
        this.internArbeidsforholdRef = Objects.requireNonNull(internArbeidsforholdRef, "internArbeidsforholdRef");
        this.uttakArbeidType = uttakArbeidType;
    }

    public AktivitetDto(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto internArbeidsforholdRef, UttakArbeidType uttakArbeidType, boolean arbeidsforholdErSplittet) {
        this.arbeidsgiver = arbeidsgiver;
        this.internArbeidsforholdRef = Objects.requireNonNull(internArbeidsforholdRef, "internArbeidsforholdRef");
        this.uttakArbeidType = uttakArbeidType;
        this.arbeidsforholdErSplittet = arbeidsforholdErSplittet;
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    public InternArbeidsforholdRefDto getInternArbeidsforholdRef() {
        return internArbeidsforholdRef;
    }

    public UttakArbeidType getUttakArbeidType() {
        return uttakArbeidType;
    }

    public boolean getArbeidsforholdErSplittet() {
        return arbeidsforholdErSplittet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
	    var that = (AktivitetDto) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
            Objects.equals(internArbeidsforholdRef, that.internArbeidsforholdRef) &&
            Objects.equals(uttakArbeidType, that.uttakArbeidType) &&
            Objects.equals(arbeidsforholdErSplittet, that.arbeidsforholdErSplittet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, internArbeidsforholdRef, uttakArbeidType, arbeidsforholdErSplittet);
    }

}
