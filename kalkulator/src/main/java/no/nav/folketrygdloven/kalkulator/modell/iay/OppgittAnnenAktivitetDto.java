package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;
import no.nav.folketrygdloven.kalkulus.kodeverk.ArbeidType;

public class OppgittAnnenAktivitetDto {

    Intervall periode;

    private ArbeidType arbeidType;

    public OppgittAnnenAktivitetDto(Intervall periode, ArbeidType arbeidType) {
        this.periode = periode;
        this.arbeidType = arbeidType;
    }

    public OppgittAnnenAktivitetDto() {
        // hibernate
    }

    public ArbeidType getArbeidType() {
        return arbeidType;
    }

    public Intervall getPeriode() {
        return periode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof OppgittAnnenAktivitetDto)) return false;
        OppgittAnnenAktivitetDto that = (OppgittAnnenAktivitetDto) o;
        return Objects.equals(periode, that.periode) &&
                Objects.equals(arbeidType, that.arbeidType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, arbeidType);
    }

    @Override
    public String toString() {
        return "AnnenAktivitetEntitet{" +
                "periode=" + periode +
                ", arbeidType=" + arbeidType +
                '}';
    }
}
