package no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn;

import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFRISINN;
import no.nav.folketrygdloven.skjæringstidspunkt.status.FastsettKombinasjoner;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettKombinasjonerFRISINN.ID)
public class FastsettKombinasjonerFRISINN extends LeafSpecification<AktivitetStatusModellFRISINN> {

    static final String ID = "FP_BR_19_4";
    static final String BESKRIVELSE = "Sett kombinasjoner";

    public FastsettKombinasjonerFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModellFRISINN regelmodell) {
        return new FastsettKombinasjoner().evaluate(regelmodell);
    }
}
