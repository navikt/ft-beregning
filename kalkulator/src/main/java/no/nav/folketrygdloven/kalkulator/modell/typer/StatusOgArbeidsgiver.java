package no.nav.folketrygdloven.kalkulator.modell.typer;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulus.kodeverk.AktivitetStatus;

public record StatusOgArbeidsgiver(AktivitetStatus aktivitetStatus, Arbeidsgiver arbeidsgiver) implements Comparable<StatusOgArbeidsgiver> {



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatusOgArbeidsgiver that = (StatusOgArbeidsgiver) o;
        return aktivitetStatus == that.aktivitetStatus && Objects.equals(arbeidsgiver, that.arbeidsgiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aktivitetStatus, arbeidsgiver);
    }

    @Override
    public int compareTo(StatusOgArbeidsgiver o) {
        var aktivitetStatusCompare = this.aktivitetStatus().compareTo(o.aktivitetStatus());
        if (aktivitetStatusCompare != 0) {
            return aktivitetStatusCompare;
        }

        if (this.arbeidsgiver() != null && o.arbeidsgiver() != null) {
            return this.arbeidsgiver().getIdentifikator().compareTo(o.arbeidsgiver().getIdentifikator());
        } else if (this.arbeidsgiver() == null) {
            return 1;
        }
        return -1;
    }
}
