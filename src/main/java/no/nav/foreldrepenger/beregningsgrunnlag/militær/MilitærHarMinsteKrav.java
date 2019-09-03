package no.nav.foreldrepenger.beregningsgrunnlag.militær;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.evaluation.RuleReasonRefImpl;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(MilitærHarMinsteKrav.ID)
public class MilitærHarMinsteKrav extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_VK_32.4";
    static final String BESKRIVELSE = "Opprett regelmerknad om totalt brutto BG < 3G og bruker har militær eller sivilforsvarstjeneste i opptjeningsperioden";
    private static final String REGELMERKNAD = "7023";

    public MilitærHarMinsteKrav() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        return nei(new RuleReasonRefImpl(REGELMERKNAD, BESKRIVELSE));
    }
}
