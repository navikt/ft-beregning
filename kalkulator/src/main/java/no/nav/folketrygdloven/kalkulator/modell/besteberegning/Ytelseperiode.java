package no.nav.folketrygdloven.kalkulator.modell.besteberegning;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import no.nav.folketrygdloven.kalkulator.tid.Intervall;

public class Ytelseperiode {
    private Intervall periode;
    private List<Ytelseandel> andeler = new ArrayList<>();

    public Ytelseperiode(Intervall periode, List<Ytelseandel> andeler) {
        Objects.requireNonNull(periode, "periode");
        Objects.requireNonNull(andeler, "andeler");
        this.periode = periode;
        this.andeler = andeler;
    }

    public Intervall getPeriode() {
        return periode;
    }

    public List<Ytelseandel> getAndeler() {
        return andeler;
    }
}
