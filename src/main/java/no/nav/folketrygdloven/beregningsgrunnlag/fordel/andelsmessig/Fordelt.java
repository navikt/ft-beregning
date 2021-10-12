package no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig;

import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelModell;
import no.nav.folketrygdloven.beregningsgrunnlag.fordel.andelsmessig.modell.FordelPeriodeModell;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class Fordelt extends LeafSpecification<FordelModell> {

    public Fordelt(){
        super("Fordelt");
    }

    @Override
    public Evaluation evaluate(FordelModell grunnlag) {
        return ja();
    }

    @Override
    public String beskrivelse() {
        return "Fordelt";
    }
}
