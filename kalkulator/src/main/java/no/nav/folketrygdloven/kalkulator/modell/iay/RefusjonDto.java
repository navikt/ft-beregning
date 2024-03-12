package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

public class RefusjonDto {

    private Beløp refusjonsbeløpMnd;
    private LocalDate fom;

    public RefusjonDto() {
    }

    public RefusjonDto(Beløp refusjonsbeløpMnd, LocalDate fom) {
        this.refusjonsbeløpMnd = refusjonsbeløpMnd;
        this.fom = fom;
    }

    RefusjonDto(RefusjonDto refusjon) {
        this.refusjonsbeløpMnd = refusjon.getRefusjonsbeløp();
        this.fom = refusjon.getFom();
    }

    public Beløp getRefusjonsbeløp() {
        return refusjonsbeløpMnd;
    }

    public LocalDate getFom() {
        return fom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof RefusjonDto)) return false;
        RefusjonDto that = (RefusjonDto) o;
        return Objects.equals(refusjonsbeløpMnd, that.refusjonsbeløpMnd) &&
                Objects.equals(fom, that.fom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(refusjonsbeløpMnd, fom);
    }

    @Override
    public String toString() {
        return "RefusjonDto{" +
                "refusjonsbeløpMnd=" + refusjonsbeløpMnd +
                ", fom=" + fom +
                '}';
    }
}
