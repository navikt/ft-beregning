package no.nav.folketrygdloven.kalkulator.steg.refusjon.modell;

import java.time.LocalDate;
import java.util.List;

public class RefusjonPeriode {
    private LocalDate fom;
    private LocalDate tom;
    private List<RefusjonAndel> andeler;

    public RefusjonPeriode(LocalDate fom, LocalDate tom, List<RefusjonAndel> andeler) {
        this.fom = fom;
        this.tom = tom;
        this.andeler = andeler;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public List<RefusjonAndel> getAndeler() {
        return andeler;
    }

    @Override
    public String toString() {
        return "RefusjonPeriode{" +
                "fom=" + fom +
                ", tom=" + tom +
                ", andeler=" + andeler +
                '}';
    }
}
