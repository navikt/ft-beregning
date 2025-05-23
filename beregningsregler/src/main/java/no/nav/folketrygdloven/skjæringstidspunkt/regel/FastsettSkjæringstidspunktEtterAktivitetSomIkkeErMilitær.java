package no.nav.folketrygdloven.skjæringstidspunkt.regel;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import no.nav.folketrygdloven.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.folketrygdloven.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettSkjæringstidspunktEtterAktivitetSomIkkeErMilitær.ID)
class FastsettSkjæringstidspunktEtterAktivitetSomIkkeErMilitær extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FP_BR 21.5";
    static final String BESKRIVELSE = "Skjæringstidspunkt for beregning settes til første dag etter siste aktivitetsdag";

    FastsettSkjæringstidspunktEtterAktivitetSomIkkeErMilitær() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        var sisteAktivitetDato = regelmodell.getAktivePerioder().stream()
            .filter(ap -> !Aktivitet.MILITÆR_ELLER_SIVILTJENESTE.equals(ap.getAktivitet()))
            .map(ap -> ap.getPeriode().getTom())
            .max(Comparator.naturalOrder())
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Skal ikke være mulig å havne her uten en aktivitet som ikke er militær"));

        var skjæringstidspunkt = sisteAktivitetDato.isBefore(regelmodell.getSkjæringstidspunktForOpptjening())
            ? sisteAktivitetDato.plusDays(1)
            : regelmodell.getSkjæringstidspunktForOpptjening();

        regelmodell.setSkjæringstidspunktForBeregning(skjæringstidspunkt);
        Map<String, Object> resultater = new HashMap<>();
        resultater.put("skjæringstidspunktForBeregning", skjæringstidspunkt);
        return beregnet(resultater);
    }
}
