package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.sykepenger;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmBeregningenGjelderSykepenger.ID)
public class SjekkOmBeregningenGjelderSykepenger extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 15.7";
    static final String BESKRIVELSE = "Gjelder beregningen ytelsen sykepenger?";

    public SjekkOmBeregningenGjelderSykepenger() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return grunnlag.getBeregningsgrunnlag().isBeregningForSykepenger() ? ja() : nei();
    }
}

