package no.nav.folketrygdloven.kalkulator.steg.besteberegning;

import java.util.List;

import no.nav.folketrygdloven.kalkulator.modell.typer.Beløp;

public class BesteberegningVurderingGrunnlag {

    private final List<BesteberegningMånedGrunnlag> seksBesteMåneder;
    private final Beløp avvikFraFørsteLedd;

    public BesteberegningVurderingGrunnlag(List<BesteberegningMånedGrunnlag> seksBesteMåneder,
                                           Beløp avvikFraFørsteLedd) {
        this.seksBesteMåneder = seksBesteMåneder;
        this.avvikFraFørsteLedd = avvikFraFørsteLedd;
    }

    public List<BesteberegningMånedGrunnlag> getSeksBesteMåneder() {
        return seksBesteMåneder;
    }

    public Beløp getAvvikFraFørsteLedd() {
        return avvikFraFørsteLedd;
    }
}
