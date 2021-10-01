package no.nav.folketrygdloven.beregningsgrunnlag.fordel;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.modell.FordelPeriodeModell;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class Fordelt extends LeafSpecification<FordelPeriodeModell> {

    public Fordelt(){
        super("Fordelt");
    }

    @Override
    public Evaluation evaluate(FordelPeriodeModell grunnlag) {
        return ja();
    }

    @Override
    public String beskrivelse() {
        return "Fordelt";
    }
}
