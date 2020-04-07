package no.nav.folketrygdloven.skjæringstidspunkt.regel.ytelse;

import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

import java.util.HashMap;
import java.util.Map;

@RuleDocumentation(KunneIkkeFastsetteSkjæringstidspunkt.ID)
class KunneIkkeFastsetteSkjæringstidspunkt extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FRISINN 1.2";
    static final String BESKRIVELSE = "Skjæringstidspunkt for beregning blir ikke satt";

    KunneIkkeFastsetteSkjæringstidspunkt() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("skjæringstidspunktForBeregning", regelmodell.getSkjæringstidspunktForBeregning());
        return beregnet(resultater);
    }
}
