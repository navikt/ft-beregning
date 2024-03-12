package no.nav.folketrygdloven.kalkulator.steg.refusjon.modell;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;
import no.nav.folketrygdloven.kalkulator.modell.typer.InternArbeidsforholdRefDto;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class RefusjonAndel {
    private AktivitetStatus aktivitetStatus;
    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRefDto arbeidsforholdRef;
    private Beløp brutto;
    private Beløp refusjon;

    public RefusjonAndel(AktivitetStatus aktivitetStatus, Arbeidsgiver arbeidsgiver, InternArbeidsforholdRefDto arbeidsforholdRef, Beløp brutto, Beløp refusjon) {
        Objects.requireNonNull(aktivitetStatus, "aktivitetStatus");
        Objects.requireNonNull(brutto, "brutto");
        Objects.requireNonNull(refusjon, "refusjon");

        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdRef = arbeidsforholdRef;
        this.brutto = brutto;
        this.refusjon = refusjon;
    }

    public Beløp getBrutto() {
        return brutto;
    }

    public Beløp getRefusjon() {
        return refusjon;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRefDto getArbeidsforholdRef() {
        return arbeidsforholdRef == null ? InternArbeidsforholdRefDto.nullRef() : arbeidsforholdRef;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public RefusjonAndelNøkkel getNøkkel() {
        return new RefusjonAndelNøkkel(aktivitetStatus, arbeidsgiver);
    }

    @Override
    public String toString() {
        return "RefusjonAndel{" +
                "aktivitetStatus=" + aktivitetStatus +
                "arbeidsgiver=" + arbeidsgiver +
                "arbeidsforholdRef=" + arbeidsforholdRef +
                ", brutto=" + brutto +
                ", refusjon=" + refusjon +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefusjonAndel that = (RefusjonAndel) o;
        return aktivitetStatus == that.aktivitetStatus && Objects.equals(arbeidsgiver, that.arbeidsgiver) && Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef) && Objects.equals(brutto, that.brutto) && Objects.equals(refusjon, that.refusjon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus, arbeidsgiver, arbeidsforholdRef, brutto, refusjon);
    }
}
