package no.nav.folketrygdloven.kalkulator.steg.refusjon.modell;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.Arbeidsgiver;
import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public class RefusjonAndelNøkkel {
    private final AktivitetStatus aktivitetStatus;
    private final Arbeidsgiver arbeidsgiver;

    public RefusjonAndelNøkkel(AktivitetStatus aktivitetStatus, Arbeidsgiver arbeidsgiver) {
        Objects.requireNonNull(aktivitetStatus, "aktivitetstatus");
        this.aktivitetStatus = aktivitetStatus;
        this.arbeidsgiver = arbeidsgiver;
    }

    public AktivitetStatus getAktivitetStatus() {
        return aktivitetStatus;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefusjonAndelNøkkel that = (RefusjonAndelNøkkel) o;
        return aktivitetStatus == that.aktivitetStatus &&
                Objects.equals(arbeidsgiver, that.arbeidsgiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus, arbeidsgiver);
    }

    @Override
    public String toString() {
        return "RefusjonAndelNøkkel{" +
                "aktivitetStatus=" + aktivitetStatus +
                ", arbeidsgiver=" + arbeidsgiver +
                '}';
    }
}
