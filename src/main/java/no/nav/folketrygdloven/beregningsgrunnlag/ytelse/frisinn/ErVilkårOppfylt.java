package no.nav.folketrygdloven.beregningsgrunnlag.ytelse.frisinn;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.fastsett.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.node.SingleEvaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(ErVilkårOppfylt.ID)
public class ErVilkårOppfylt extends LeafSpecification<BeregningsgrunnlagPeriode> {

    public static final String ID = "FRISINN 6.10";
    public static final String BESKRIVELSE = "Er vilkåret oppfylt for perioden?";

    public ErVilkårOppfylt() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        boolean erVilkårOppfylt = grunnlag.getErVilkårOppfylt();
        SingleEvaluation resultat = erVilkårOppfylt ? ja() : nei();
        resultat.setEvaluationProperty("erVilkårOppfylt", erVilkårOppfylt);
        return resultat;
    }
}
