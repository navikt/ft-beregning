package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import java.time.YearMonth;
import java.util.List;


public class BesteberegningMånedGrunnlag {

    private final List<Inntekt> inntekter;
    private final YearMonth måned;

    public BesteberegningMånedGrunnlag(List<Inntekt> inntekter, YearMonth måned) {
        this.inntekter = inntekter;
        this.måned = måned;
    }

    public List<Inntekt> getInntekter() {
        return inntekter;
    }

    public YearMonth getMåned() {
        return måned;
    }
}
