package no.nav.folketrygdloven.skjæringstidspunkt.regel;

import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkOmAktivitetRettFørOpptjening.ID)
public class SjekkOmAktivitetRettFørOpptjening extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FP_BR 21.1";
    static final String BESKRIVELSE = "Er det aktivitet frem til skjæringstidspunkt for opptjening?";

    public SjekkOmAktivitetRettFørOpptjening() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        return regelmodell.getSkjæringstidspunktForOpptjening().minusDays(1).isBefore(regelmodell.sisteAktivitetsdato()) ? ja() : nei();
    }
}
