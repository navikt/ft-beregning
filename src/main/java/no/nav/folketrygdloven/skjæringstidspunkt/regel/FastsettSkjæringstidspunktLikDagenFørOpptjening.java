package no.nav.folketrygdloven.skjæringstidspunkt.regel;

import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettSkjæringstidspunktLikDagenFørOpptjening.ID)
public class FastsettSkjæringstidspunktLikDagenFørOpptjening extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FP_BR 21.6";
    static final String BESKRIVELSE = "Skjæringstidspunkt for beregning settes lik dagen før skjæringstidspunkt for opptjening";

    public FastsettSkjæringstidspunktLikDagenFørOpptjening() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        regelmodell.setSkjæringstidspunktForBeregning(regelmodell.getSkjæringstidspunktForOpptjening().minusDays(1));
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("skjæringstidspunktForBeregning", regelmodell.getSkjæringstidspunktForBeregning());
        return beregnet(resultater);
    }
}
