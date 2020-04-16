package no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn;

import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFRISINN;
import no.nav.folketrygdloven.skjæringstidspunkt.status.FastsettStatusForBeregningsgrunnlag;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettStatusForBeregningsgrunnlagFRISINN.ID)
public class FastsettStatusForBeregningsgrunnlagFRISINN extends LeafSpecification<AktivitetStatusModellFRISINN> {

    static final String ID = "FP_BR 19.5";
    static final String BESKRIVELSE = "Fastsett status for beregningsgrunnlag";

    public FastsettStatusForBeregningsgrunnlagFRISINN(){
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModellFRISINN regelmodell) {
        return new FastsettStatusForBeregningsgrunnlag().evaluate(regelmodell);
    }
}
