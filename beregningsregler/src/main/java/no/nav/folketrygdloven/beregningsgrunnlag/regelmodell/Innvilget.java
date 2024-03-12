package no.nav.folketrygdloven.beregningsgrunnlag.regelmodell;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

public class Innvilget extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public Innvilget(){
        super("Innvilget");
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return ja();
    }

    @Override
    public String beskrivelse() {
        return "Innvilget";
    }
}
