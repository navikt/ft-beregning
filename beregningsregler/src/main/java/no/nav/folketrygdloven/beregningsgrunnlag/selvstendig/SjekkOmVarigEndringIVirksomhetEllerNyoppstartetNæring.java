package no.nav.folketrygdloven.beregningsgrunnlag.selvstendig;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.resultat.BeregningsgrunnlagPeriode;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmVarigEndringIVirksomhetEllerNyoppstartetNæring.ID)
public class SjekkOmVarigEndringIVirksomhetEllerNyoppstartetNæring extends LeafSpecification<BeregningsgrunnlagPeriode> {

    static final String ID = "FP_BR 2.3";
    static final String BESKRIVELSE = "Har bruker oppgitt varig endring i virksomhet eller nyoppstartet virksomhet?";

    public SjekkOmVarigEndringIVirksomhetEllerNyoppstartetNæring() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(BeregningsgrunnlagPeriode grunnlag) {
        var månedsinntekt = grunnlag.getInntektsgrunnlag().getSistePeriodeinntektMedTypeSøknad();
        return (månedsinntekt.isPresent() ? ja() : nei());
    }
}
