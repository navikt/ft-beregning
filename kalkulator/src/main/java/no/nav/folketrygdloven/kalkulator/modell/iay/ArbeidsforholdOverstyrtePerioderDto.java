package no.nav.folketrygdloven.kalkulator.modell.iay;

import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class ArbeidsforholdOverstyrtePerioderDto {

    private Intervall periode;

    ArbeidsforholdOverstyrtePerioderDto() {

    }

    ArbeidsforholdOverstyrtePerioderDto(ArbeidsforholdOverstyrtePerioderDto arbeidsforholdOverstyrtePerioder) {
        this.periode = arbeidsforholdOverstyrtePerioder.getOverstyrtePeriode();
    }

    void setPeriode(Intervall periode) {
        this.periode = periode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ArbeidsforholdOverstyrtePerioderDto)) return false;
        ArbeidsforholdOverstyrtePerioderDto that = (ArbeidsforholdOverstyrtePerioderDto) o;
        return Objects.equals(periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode);
    }

    @Override
    public String toString() {
        return "ArbeidsforholdInformasjonEntitet{" +
                "periode=" + periode +
                '}';
    }

    public Intervall getOverstyrtePeriode() {
        return periode;
    }
}
