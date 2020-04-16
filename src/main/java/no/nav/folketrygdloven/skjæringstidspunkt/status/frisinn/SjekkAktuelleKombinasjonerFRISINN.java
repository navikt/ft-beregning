package no.nav.folketrygdloven.skjæringstidspunkt.status.frisinn;

import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModellFRISINN;
import no.nav.folketrygdloven.skjæringstidspunkt.status.SjekkAktuelleKombinasjoner;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(SjekkAktuelleKombinasjonerFRISINN.ID)
public class SjekkAktuelleKombinasjonerFRISINN extends LeafSpecification<AktivitetStatusModellFRISINN> {

    static final String ID = "FP_BR_19_3";
    static final String BESKRIVELSE = "Sjekk aktuelle kombinasjoner?";

    public SjekkAktuelleKombinasjonerFRISINN() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModellFRISINN regelmodell) {
        return new SjekkAktuelleKombinasjoner().evaluate(regelmodell);
    }
}
